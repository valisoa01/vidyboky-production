package com.example.demo.librairie.service;

import com.example.demo.librairie.entity.Author;
import com.example.demo.librairie.entity.Book;
import com.example.demo.librairie.repository.AuthorRepository;
import com.example.demo.librairie.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookExternalServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private BookExternalService bookExternalService;

    private final String isbn = "9782070415755";
    private final String apiUrl = "https://openlibrary.org/api/books";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bookExternalService, "baseApiUrl", apiUrl);
    }

    @Test
    void shouldReturnLocalBookIfExists() {
        Book mockBook = Book.builder()
                .isbn(isbn)
                .title("Les Misérables")
                .build();
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(mockBook));

        Book result = bookExternalService.getOrFetchByIsbn(isbn);

        assertNotNull(result);
        assertEquals("Les Misérables", result.getTitle());
        verify(bookRepository, times(1)).findByIsbn(isbn);
        verifyNoInteractions(restTemplate); // Should not call the external API
    }

    @Test
    void shouldFetchAndSaveBookFromApiIfAbsentLocally() {
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        String jsonMockResponse = "{"
                + "\"ISBN:" + isbn + "\": {"
                + "\"title\": \"Le Petit Prince\","
                + "\"publish_date\": \"1943\","
                + "\"description\": \"A beautiful story.\","
                + "\"authors\": [{\"name\": \"Antoine de Saint-Exupéry\"}]"
                + "}"
                + "}";

        String expectedUrl = String.format("%s?bibkeys=ISBN:%s&format=json&jscmd=data", apiUrl, isbn);
        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(jsonMockResponse);

        when(authorRepository.findByFullName("Antoine de Saint-Exupéry")).thenReturn(Optional.empty());
        Author savedAuthor = Author.builder().fullName("Antoine de Saint-Exupéry").build();
        when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);

        Book expectedSavedBook = Book.builder()
                .title("Le Petit Prince")
                .isbn(isbn)
                .publicationDate(LocalDate.of(1943, 1, 1))
                .build();
        when(bookRepository.save(any(Book.class))).thenReturn(expectedSavedBook);

        Book result = bookExternalService.getOrFetchByIsbn(isbn);

        assertNotNull(result);
        assertEquals("Le Petit Prince", result.getTitle());
        verify(bookRepository, times(1)).save(any(Book.class));
        verify(authorRepository, times(1)).save(any(Author.class));
    }

    @Test
    void shouldThrowExceptionWhenBookNotFoundOnExternalApi() {
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        String emptyResponse = "{}";
        String expectedUrl = String.format("%s?bibkeys=ISBN:%s&format=json&jscmd=data", apiUrl, isbn);
        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(emptyResponse);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookExternalService.getOrFetchByIsbn(isbn);
        });

        assertTrue(exception.getMessage().contains("Book not found with ISBN"));
    }
}