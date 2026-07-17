package com.example.demo.librairie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.demo.librairie.dto.GoogleBooksResponse;
import com.example.demo.librairie.entity.Author;
import com.example.demo.librairie.entity.Book;
import com.example.demo.librairie.exception.ExternalServiceException;
import com.example.demo.librairie.exception.ResourceNotFoundException;
import com.example.demo.librairie.repository.AuthorRepository;
import com.example.demo.librairie.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class BookExternalServiceTest {

  @Mock private BookRepository bookRepository;

  @Mock private AuthorRepository authorRepository;

  @Mock private RestTemplate restTemplate;

  @Spy private ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks private BookExternalService bookExternalService;

  private final String isbn = "9782070415755";
  private final String openLibraryApiUrl = "https://openlibrary.org/api/books";
  private final String googleBooksApiUrl = "https://www.googleapis.com/books/v1/volumes";

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(bookExternalService, "baseApiUrl", openLibraryApiUrl);
    ReflectionTestUtils.setField(bookExternalService, "googleBooksApiUrl", googleBooksApiUrl);
  }

  private String openLibraryUrl() {
    return String.format("%s?bibkeys=ISBN:%s&format=json&jscmd=data", openLibraryApiUrl, isbn);
  }

  private String googleBooksUrl() {
    return String.format("%s?q=isbn:%s", googleBooksApiUrl, isbn);
  }

  @Test
  void shouldReturnLocalBookIfExists() {
    Book mockBook = Book.builder().isbn(isbn).title("Les Misérables").build();
    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(mockBook));

    Book result = bookExternalService.getOrFetchByIsbn(isbn);

    assertNotNull(result);
    assertEquals("Les Misérables", result.getTitle());
    verify(bookRepository, times(1)).findByIsbn(isbn);
    verifyNoInteractions(restTemplate);
  }

  @Test
  void shouldFetchAndSaveBookFromOpenLibraryWhenGoogleBooksHasNoResult() {
    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

    String openLibraryJson =
        "{"
            + "\"ISBN:"
            + isbn
            + "\": {"
            + "\"title\": \"Le Petit Prince\","
            + "\"publish_date\": \"1943\","
            + "\"description\": \"A beautiful story.\","
            + "\"authors\": [{\"name\": \"Antoine de Saint-Exupéry\"}]"
            + "}"
            + "}";
    when(restTemplate.getForObject(openLibraryUrl(), String.class)).thenReturn(openLibraryJson);
    when(restTemplate.getForObject(googleBooksUrl(), GoogleBooksResponse.class))
        .thenReturn(new GoogleBooksResponse()); // no items

    when(authorRepository.findByFullName("Antoine de Saint-Exupéry")).thenReturn(Optional.empty());
    Author savedAuthor = Author.builder().fullName("Antoine de Saint-Exupéry").build();
    when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);

    Book expectedSavedBook =
        Book.builder()
            .title("Le Petit Prince")
            .isbn(isbn)
            .publicationDate(LocalDate.of(1943, 1, 1))
            .build();
    when(bookRepository.save(any(Book.class))).thenReturn(expectedSavedBook);

    Book result = bookExternalService.getOrFetchByIsbn(isbn);

    assertNotNull(result);
    assertEquals("Le Petit Prince", result.getTitle());
    verify(bookRepository, times(1)).save(any(Book.class));
  }

  @Test
  void shouldFallBackToGoogleBooksWhenOpenLibraryHasNoResult() {
    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

    // Open Library returns an empty payload
    when(restTemplate.getForObject(openLibraryUrl(), String.class)).thenReturn("{}");

    GoogleBooksResponse googleResponse = new GoogleBooksResponse();
    GoogleBooksResponse.VolumeInfo volumeInfo = new GoogleBooksResponse.VolumeInfo();
    volumeInfo.setTitle("Le Petit Prince");
    volumeInfo.setPublishedDate("1943-04-06");
    volumeInfo.setDescription("A tale of a young prince.");
    volumeInfo.setAuthors(List.of("Antoine de Saint-Exupéry"));
    GoogleBooksResponse.Item item = new GoogleBooksResponse.Item();
    item.setVolumeInfo(volumeInfo);
    googleResponse.setItems(List.of(item));

    when(restTemplate.getForObject(googleBooksUrl(), GoogleBooksResponse.class))
        .thenReturn(googleResponse);

    when(authorRepository.findByFullName("Antoine de Saint-Exupéry")).thenReturn(Optional.empty());
    Author savedAuthor = Author.builder().fullName("Antoine de Saint-Exupéry").build();
    when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);

    Book expectedSavedBook = Book.builder().title("Le Petit Prince").isbn(isbn).build();
    when(bookRepository.save(any(Book.class))).thenReturn(expectedSavedBook);

    Book result = bookExternalService.getOrFetchByIsbn(isbn);

    assertNotNull(result);
    assertEquals("Le Petit Prince", result.getTitle());
    verify(bookRepository, times(1)).save(any(Book.class));
  }

  @Test
  void shouldThrowResourceNotFoundExceptionWhenBothApisHaveNoResult() {
    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

    when(restTemplate.getForObject(openLibraryUrl(), String.class)).thenReturn("{}");
    when(restTemplate.getForObject(googleBooksUrl(), GoogleBooksResponse.class))
        .thenReturn(new GoogleBooksResponse());

    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class, () -> bookExternalService.getOrFetchByIsbn(isbn));

    assertTrue(exception.getMessage().contains("Book not found with ISBN"));
    verify(bookRepository, never()).save(any());
  }

  @Test
  void shouldSucceedWhenGoogleBooksFailsButOpenLibrarySucceeds() {
    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

    String openLibraryJson =
        "{" + "\"ISBN:" + isbn + "\": {" + "\"title\": \"Le Petit Prince\"" + "}" + "}";
    when(restTemplate.getForObject(openLibraryUrl(), String.class)).thenReturn(openLibraryJson);
    // Une vraie panne réseau côté RestTemplate ressemble à ça, pas à un RuntimeException brut.
    when(restTemplate.getForObject(googleBooksUrl(), GoogleBooksResponse.class))
        .thenThrow(new ResourceAccessException("Google Books is down"));

    Book expectedSavedBook = Book.builder().title("Le Petit Prince").isbn(isbn).build();
    when(bookRepository.save(any(Book.class))).thenReturn(expectedSavedBook);

    Book result = bookExternalService.getOrFetchByIsbn(isbn);

    assertNotNull(result);
    assertEquals("Le Petit Prince", result.getTitle());
  }

  @Test
  void shouldThrowExternalServiceExceptionWhenBothProvidersFailTechnically() {
    when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

    when(restTemplate.getForObject(openLibraryUrl(), String.class))
        .thenThrow(new ResourceAccessException("Open Library is down"));
    when(restTemplate.getForObject(googleBooksUrl(), GoogleBooksResponse.class))
        .thenThrow(new ResourceAccessException("Google Books is down"));

    // Les deux fournisseurs sont en panne : ce n'est PAS un "livre introuvable" (404),
    // mais une vraie panne technique qui doit remonter comme telle (502).
    assertThrows(ExternalServiceException.class, () -> bookExternalService.getOrFetchByIsbn(isbn));

    verify(bookRepository, never()).save(any());
  }

  @Test
  void shouldRejectBlankIsbn() {
    assertThrows(IllegalArgumentException.class, () -> bookExternalService.getOrFetchByIsbn("  "));

    verifyNoInteractions(bookRepository, restTemplate);
  }
}
