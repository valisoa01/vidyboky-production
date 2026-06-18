package com.example.demo.librairie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.librairie.dto.BookRequest;
import com.example.demo.librairie.entity.Author;
import com.example.demo.librairie.entity.Book;
import com.example.demo.librairie.entity.Genre;
import com.example.demo.librairie.repository.AuthorRepository;
import com.example.demo.librairie.repository.BookRepository;
import com.example.demo.librairie.repository.GenreRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

  @Mock private BookRepository bookRepository;

  @Mock private AuthorRepository authorRepository;

  @Mock private GenreRepository genreRepository;

  @InjectMocks private BookService bookService;

  private UUID bookId;
  private UUID authorId;
  private UUID genreId;
  private Book book;
  private BookRequest bookRequest;
  private Author author;
  private Genre genre;

  @BeforeEach
  void setUp() {
    bookId = UUID.randomUUID();
    authorId = UUID.randomUUID();
    genreId = UUID.randomUUID();

    author = Author.builder().id(authorId).fullName("John Doe").build();

    genre = Genre.builder().id(genreId).name("Fiction").description("Fiction books").build();

    book =
        Book.builder()
            .id(bookId)
            .title("Test Book")
            .isbn("1234567890")
            .description("Test Description")
            .publicationDate(LocalDate.of(2024, 1, 1))
            .creationDate(LocalDate.now())
            .authors(List.of(author))
            .genres(List.of(genre))
            .build();

    bookRequest =
        BookRequest.builder()
            .title("Test Book")
            .isbn("1234567890")
            .description("Test Description")
            .publicationDate(LocalDate.of(2024, 1, 1))
            .authorIds(List.of(authorId))
            .genreIds(List.of(genreId))
            .build();
  }

  @Test
  void getAll() {
    when(bookRepository.findAll()).thenReturn(List.of(book));

    List<Book> result = bookService.getAll();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(book.getTitle(), result.get(0).getTitle());
    verify(bookRepository, times(1)).findAll();
  }

  @Test
  void getById() {
    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

    Book result = bookService.getById(bookId);

    assertNotNull(result);
    assertEquals(book.getTitle(), result.getTitle());
    verify(bookRepository, times(1)).findById(bookId);
  }

  @Test
  void getById_ShouldThrowException_WhenNotFound() {
    when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> bookService.getById(bookId));
    verify(bookRepository, times(1)).findById(bookId);
  }

  @Test
  void getLivreByTitle() {
    when(bookRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(List.of(book));

    List<Book> result = bookService.getLivreByTitle("Test");

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(bookRepository, times(1)).findByTitleContainingIgnoreCase("Test");
  }

  @Test
  void getLivreByGenre() {
    when(bookRepository.findByGenres_Id(genreId)).thenReturn(List.of(book));

    List<Book> result = bookService.getLivreByGenre(genreId);

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(bookRepository, times(1)).findByGenres_Id(genreId);
  }

  @Test
  void getLivreByDate() {
    LocalDate date = LocalDate.of(2024, 1, 1);
    when(bookRepository.findByPublicationDate(date)).thenReturn(List.of(book));

    List<Book> result = bookService.getLivreByDate(date);

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(bookRepository, times(1)).findByPublicationDate(date);
  }

  @Test
  void createLivre() {
    when(authorRepository.findAllById(List.of(authorId))).thenReturn(List.of(author));
    when(genreRepository.findAllById(List.of(genreId))).thenReturn(List.of(genre));
    when(bookRepository.save(any(Book.class))).thenReturn(book);

    Book result = bookService.createLivre(bookRequest);

    assertNotNull(result);
    assertEquals(bookRequest.getTitle(), result.getTitle());
    assertNotNull(result.getCreationDate());
    verify(authorRepository, times(1)).findAllById(List.of(authorId));
    verify(genreRepository, times(1)).findAllById(List.of(genreId));
    verify(bookRepository, times(1)).save(any(Book.class));
  }

  @Test
  void updateLivre() {
    BookRequest updateRequest =
        BookRequest.builder()
            .title("Updated Title")
            .isbn("0987654321")
            .description("Updated Description")
            .publicationDate(LocalDate.of(2024, 2, 1))
            .authorIds(List.of(authorId))
            .genreIds(List.of(genreId))
            .build();

    Book updatedBook =
        Book.builder()
            .id(bookId)
            .title("Updated Title")
            .isbn("0987654321")
            .description("Updated Description")
            .publicationDate(LocalDate.of(2024, 2, 1))
            .creationDate(book.getCreationDate())
            .authors(List.of(author))
            .genres(List.of(genre))
            .build();

    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
    when(authorRepository.findAllById(List.of(authorId))).thenReturn(List.of(author));
    when(genreRepository.findAllById(List.of(genreId))).thenReturn(List.of(genre));
    when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

    Book result = bookService.updateLivre(bookId, updateRequest);

    assertNotNull(result);
    assertEquals(updateRequest.getTitle(), result.getTitle());
    verify(bookRepository, times(1)).findById(bookId);
    verify(bookRepository, times(1)).save(any(Book.class));
  }

  @Test
  void deleteLivre() {
    when(bookRepository.existsById(bookId)).thenReturn(true);

    assertDoesNotThrow(() -> bookService.deleteLivre(bookId));
    verify(bookRepository, times(1)).existsById(bookId);
    verify(bookRepository, times(1)).deleteById(bookId);
  }

  @Test
  void deleteLivre_ShouldThrowException_WhenNotFound() {
    when(bookRepository.existsById(bookId)).thenReturn(false);

    assertThrows(RuntimeException.class, () -> bookService.deleteLivre(bookId));
    verify(bookRepository, times(1)).existsById(bookId);
    verify(bookRepository, never()).deleteById(bookId);
  }
}
