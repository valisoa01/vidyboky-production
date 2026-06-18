package com.example.demo.librairie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.librairie.dto.BookFormatRequestDTO;
import com.example.demo.librairie.dto.BookFormatResponseDTO;
import com.example.demo.librairie.entity.Book;
import com.example.demo.librairie.entity.BookFormat;
import com.example.demo.librairie.entity.Format;
import com.example.demo.librairie.repository.BookFormatRepository;
import com.example.demo.librairie.repository.BookRepository;
import com.example.demo.librairie.repository.FormatRepository;
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
class BookFormatServiceTest {

  @Mock private BookFormatRepository bookFormatRepository;

  @Mock private BookRepository bookRepository;

  @Mock private FormatRepository formatRepository;

  @InjectMocks private BookFormatService bookFormatService;

  private UUID bookFormatId;
  private UUID bookId;
  private UUID formatId;
  private BookFormat bookFormat;
  private Book book;
  private Format format;
  private BookFormatRequestDTO request;

  @BeforeEach
  void setUp() {
    bookFormatId = UUID.randomUUID();
    bookId = UUID.randomUUID();
    formatId = UUID.randomUUID();

    book = Book.builder().id(bookId).title("Test Book").build();

    format = Format.builder().id(formatId).formatType("Paperback").build();

    bookFormat =
        BookFormat.builder().id(bookFormatId).book(book).format(format).price((19.99)).build();

    request =
        BookFormatRequestDTO.builder().bookId(bookId).formatId(formatId).price((19.99)).build();
  }

  @Test
  void create_ShouldReturnCreatedBookFormat_WhenValidRequest() {
    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
    when(formatRepository.findById(formatId)).thenReturn(Optional.of(format));
    when(bookFormatRepository.existsByBookIdAndFormatId(bookId, formatId)).thenReturn(false);
    when(bookFormatRepository.save(any(BookFormat.class))).thenReturn(bookFormat);

    BookFormatResponseDTO result = bookFormatService.create(request);

    assertNotNull(result);
    assertEquals(bookFormat.getPrice(), result.getPrice());
    assertEquals(book.getId(), result.getBookId());
    assertEquals(book.getTitle(), result.getBookTitle());
    assertEquals(format.getId(), result.getFormatId());
    assertEquals(format.getFormatType(), result.getTypeFormat());

    verify(bookRepository, times(1)).findById(bookId);
    verify(formatRepository, times(1)).findById(formatId);
    verify(bookFormatRepository, times(1)).existsByBookIdAndFormatId(bookId, formatId);
    verify(bookFormatRepository, times(1)).save(any(BookFormat.class));
  }

  @Test
  void create_ShouldThrowException_WhenBookNotFound() {
    when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> bookFormatService.create(request));

    assertEquals("Book not found", exception.getMessage());
    verify(bookRepository, times(1)).findById(bookId);
    verify(formatRepository, never()).findById(any());
    verify(bookFormatRepository, never()).existsByBookIdAndFormatId(any(), any());
    verify(bookFormatRepository, never()).save(any(BookFormat.class));
  }

  @Test
  void create_ShouldThrowException_WhenFormatNotFound() {
    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
    when(formatRepository.findById(formatId)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> bookFormatService.create(request));

    assertEquals("Format not found", exception.getMessage());
    verify(bookRepository, times(1)).findById(bookId);
    verify(formatRepository, times(1)).findById(formatId);
    verify(bookFormatRepository, never()).existsByBookIdAndFormatId(any(), any());
    verify(bookFormatRepository, never()).save(any(BookFormat.class));
  }

  @Test
  void create_ShouldThrowException_WhenBookFormatAlreadyExists() {
    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
    when(formatRepository.findById(formatId)).thenReturn(Optional.of(format));
    when(bookFormatRepository.existsByBookIdAndFormatId(bookId, formatId)).thenReturn(true);

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> bookFormatService.create(request));

    assertEquals("This book already has this format", exception.getMessage());
    verify(bookRepository, times(1)).findById(bookId);
    verify(formatRepository, times(1)).findById(formatId);
    verify(bookFormatRepository, times(1)).existsByBookIdAndFormatId(bookId, formatId);
    verify(bookFormatRepository, never()).save(any(BookFormat.class));
  }

  @Test
  void findAll_ShouldReturnListOfBookFormats() {
    when(bookFormatRepository.findAll()).thenReturn(List.of(bookFormat));

    List<BookFormatResponseDTO> result = bookFormatService.findAll();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(bookFormat.getPrice(), result.get(0).getPrice());
    assertEquals(book.getId(), result.get(0).getBookId());
    assertEquals(format.getId(), result.get(0).getFormatId());
    verify(bookFormatRepository, times(1)).findAll();
  }

  @Test
  void findAll_ShouldReturnEmptyList_WhenNoBookFormats() {
    when(bookFormatRepository.findAll()).thenReturn(List.of());

    List<BookFormatResponseDTO> result = bookFormatService.findAll();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(bookFormatRepository, times(1)).findAll();
  }

  @Test
  void findById_ShouldReturnBookFormat_WhenExists() {
    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));

    BookFormatResponseDTO result = bookFormatService.findById(bookFormatId);

    assertNotNull(result);
    assertEquals(bookFormat.getPrice(), result.getPrice());
    assertEquals(book.getId(), result.getBookId());
    assertEquals(format.getId(), result.getFormatId());
    verify(bookFormatRepository, times(1)).findById(bookFormatId);
  }

  @Test
  void findById_ShouldThrowException_WhenNotFound() {
    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> bookFormatService.findById(bookFormatId));

    assertEquals("BookFormat not found", exception.getMessage());
    verify(bookFormatRepository, times(1)).findById(bookFormatId);
  }

  @Test
  void update_ShouldReturnUpdatedBookFormat_WhenExists() {
    BookFormatRequestDTO updateRequest =
        BookFormatRequestDTO.builder().bookId(bookId).formatId(formatId).price((29.99)).build();

    BookFormat updatedBookFormat =
        BookFormat.builder().id(bookFormatId).book(book).format(format).price((29.99)).build();

    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));
    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
    when(formatRepository.findById(formatId)).thenReturn(Optional.of(format));
    when(bookFormatRepository.save(any(BookFormat.class))).thenReturn(updatedBookFormat);

    BookFormatResponseDTO result = bookFormatService.update(bookFormatId, updateRequest);

    assertNotNull(result);
    assertEquals(updateRequest.getPrice(), result.getPrice());
    verify(bookFormatRepository, times(1)).findById(bookFormatId);
    verify(bookRepository, times(1)).findById(bookId);
    verify(formatRepository, times(1)).findById(formatId);
    verify(bookFormatRepository, times(1)).save(any(BookFormat.class));
  }

  @Test
  void update_ShouldThrowException_WhenBookFormatNotFound() {
    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> bookFormatService.update(bookFormatId, request));

    assertEquals("BookFormat not found", exception.getMessage());
    verify(bookFormatRepository, times(1)).findById(bookFormatId);
    verify(bookRepository, never()).findById(any());
    verify(formatRepository, never()).findById(any());
    verify(bookFormatRepository, never()).save(any(BookFormat.class));
  }

  @Test
  void update_ShouldThrowException_WhenBookNotFound() {
    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));
    when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> bookFormatService.update(bookFormatId, request));

    assertEquals("Book not found", exception.getMessage());
    verify(bookFormatRepository, times(1)).findById(bookFormatId);
    verify(bookRepository, times(1)).findById(bookId);
    verify(formatRepository, never()).findById(any());
    verify(bookFormatRepository, never()).save(any(BookFormat.class));
  }

  @Test
  void update_ShouldThrowException_WhenFormatNotFound() {
    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));
    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
    when(formatRepository.findById(formatId)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> bookFormatService.update(bookFormatId, request));

    assertEquals("Format not found", exception.getMessage());
    verify(bookFormatRepository, times(1)).findById(bookFormatId);
    verify(bookRepository, times(1)).findById(bookId);
    verify(formatRepository, times(1)).findById(formatId);
    verify(bookFormatRepository, never()).save(any(BookFormat.class));
  }

  @Test
  void delete_ShouldDeleteBookFormat_WhenExists() {
    when(bookFormatRepository.existsById(bookFormatId)).thenReturn(true);

    assertDoesNotThrow(() -> bookFormatService.delete(bookFormatId));
    verify(bookFormatRepository, times(1)).existsById(bookFormatId);
    verify(bookFormatRepository, times(1)).deleteById(bookFormatId);
  }

  @Test
  void delete_ShouldThrowException_WhenNotFound() {
    when(bookFormatRepository.existsById(bookFormatId)).thenReturn(false);

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> bookFormatService.delete(bookFormatId));

    assertEquals("BookFormat not found", exception.getMessage());
    verify(bookFormatRepository, times(1)).existsById(bookFormatId);
    verify(bookFormatRepository, never()).deleteById(bookFormatId);
  }
}
