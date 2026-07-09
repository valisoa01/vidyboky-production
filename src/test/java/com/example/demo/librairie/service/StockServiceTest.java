package com.example.demo.librairie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.librairie.dto.StockRequest;
import com.example.demo.librairie.dto.StockResponse;
import com.example.demo.librairie.entity.Book;
import com.example.demo.librairie.entity.BookFormat;
import com.example.demo.librairie.entity.Format;
import com.example.demo.librairie.entity.MovementType;
import com.example.demo.librairie.entity.StockMovement;
import com.example.demo.librairie.repository.BookFormatRepository;
import com.example.demo.librairie.repository.StockRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

  @Mock private StockRepository stockRepository;

  @Mock private BookFormatRepository bookFormatRepository;

  @InjectMocks private StockService stockService;

  private UUID stockId;
  private UUID bookFormatId;
  private UUID bookId;
  private UUID formatId;
  private Book book;
  private Format format;
  private BookFormat bookFormat;
  private StockMovement stockMovementIn;
  private StockMovement stockMovementOut;

  @BeforeEach
  void setUp() {
    stockId = UUID.randomUUID();
    bookFormatId = UUID.randomUUID();
    bookId = UUID.randomUUID();
    formatId = UUID.randomUUID();

    book = Book.builder().id(bookId).title("Test Book").build();
    format = Format.builder().id(formatId).formatType("Paperback").build();
    bookFormat =
        BookFormat.builder().id(bookFormatId).book(book).format(format).price(19.99).build();

    stockMovementIn =
        StockMovement.builder()
            .id(stockId)
            .movement(MovementType.IN)
            .quantity(10)
            .movementDate(LocalDateTime.now())
            .bookFormat(bookFormat)
            .build();

    stockMovementOut =
        StockMovement.builder()
            .id(UUID.randomUUID())
            .movement(MovementType.OUT)
            .quantity(4)
            .movementDate(LocalDateTime.now())
            .bookFormat(bookFormat)
            .build();
  }

  @Test
  void getAll_ShouldReturnListOfStockResponses() {
    when(stockRepository.findAll()).thenReturn(List.of(stockMovementIn, stockMovementOut));

    List<StockResponse> result = stockService.getAll();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(stockMovementIn.getQuantity(), result.get(0).getQuantity());
    assertEquals(book.getTitle(), result.get(0).getBookTitle());
    assertEquals(format.getFormatType(), result.get(0).getFormatType());
    verify(stockRepository, times(1)).findAll();
  }

  @Test
  void getAll_ShouldReturnEmptyList_WhenNoStockMovements() {
    when(stockRepository.findAll()).thenReturn(List.of());

    List<StockResponse> result = stockService.getAll();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(stockRepository, times(1)).findAll();
  }

  @Test
  void getById_ShouldReturnStockResponse_WhenExists() {
    when(stockRepository.findById(stockId)).thenReturn(Optional.of(stockMovementIn));

    StockResponse result = stockService.getById(stockId);

    assertNotNull(result);
    assertEquals(stockId, result.getId());
    assertEquals(MovementType.IN, result.getMovement());
    assertEquals(bookFormatId, result.getBookFormatId());
    verify(stockRepository, times(1)).findById(stockId);
  }

  @Test
  void getById_ShouldThrowException_WhenNotFound() {
    when(stockRepository.findById(stockId)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> stockService.getById(stockId));

    assertEquals("Stock not found with id: " + stockId, exception.getMessage());
    verify(stockRepository, times(1)).findById(stockId);
  }

  @Test
  void getByBookFormatId_ShouldReturnList_WhenBookFormatExists() {
    when(bookFormatRepository.existsById(bookFormatId)).thenReturn(true);
    when(stockRepository.findByBookFormatId(bookFormatId))
        .thenReturn(List.of(stockMovementIn, stockMovementOut));

    List<StockResponse> result = stockService.getByBookFormatId(bookFormatId);

    assertNotNull(result);
    assertEquals(2, result.size());
    verify(bookFormatRepository, times(1)).existsById(bookFormatId);
    verify(stockRepository, times(1)).findByBookFormatId(bookFormatId);
  }

  @Test
  void getByBookFormatId_ShouldThrowException_WhenBookFormatNotFound() {
    when(bookFormatRepository.existsById(bookFormatId)).thenReturn(false);

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> stockService.getByBookFormatId(bookFormatId));

    assertEquals("BookFormat not found with id: " + bookFormatId, exception.getMessage());
    verify(bookFormatRepository, times(1)).existsById(bookFormatId);
    verify(stockRepository, never()).findByBookFormatId(any());
  }

  @Test
  void getCurrentStock_ShouldReturnComputedSum_WhenBookFormatExists() {
    when(bookFormatRepository.existsById(bookFormatId)).thenReturn(true);
    when(stockRepository.findByBookFormatId(bookFormatId))
        .thenReturn(List.of(stockMovementIn, stockMovementOut));

    Integer result = stockService.getCurrentStock(bookFormatId);

    assertEquals(6, result);
    verify(bookFormatRepository, times(1)).existsById(bookFormatId);
    verify(stockRepository, times(1)).findByBookFormatId(bookFormatId);
  }

  @Test
  void getCurrentStock_ShouldReturnZero_WhenNoMovements() {
    when(bookFormatRepository.existsById(bookFormatId)).thenReturn(true);
    when(stockRepository.findByBookFormatId(bookFormatId)).thenReturn(List.of());

    Integer result = stockService.getCurrentStock(bookFormatId);

    assertEquals(0, result);
    verify(stockRepository, times(1)).findByBookFormatId(bookFormatId);
  }

  @Test
  void getCurrentStock_ShouldThrowException_WhenBookFormatNotFound() {
    when(bookFormatRepository.existsById(bookFormatId)).thenReturn(false);

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> stockService.getCurrentStock(bookFormatId));

    assertEquals("BookFormat not found with id: " + bookFormatId, exception.getMessage());
    verify(bookFormatRepository, times(1)).existsById(bookFormatId);
    verify(stockRepository, never()).findByBookFormatId(any());
  }

  @Test
  void create_ShouldSaveInMovement_WhenBookFormatExists() {
    StockRequest request =
        StockRequest.builder()
            .bookFormatId(bookFormatId)
            .movement(MovementType.IN)
            .quantity(15)
            .build();

    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));
    when(stockRepository.save(any(StockMovement.class))).thenReturn(stockMovementIn);

    StockResponse result = stockService.create(request);

    assertNotNull(result);
    assertEquals(MovementType.IN, result.getMovement());
    verify(bookFormatRepository, times(1)).findById(bookFormatId);
    verify(stockRepository, times(1)).save(any(StockMovement.class));
    verify(bookFormatRepository, never()).existsById(any());
  }

  @Test
  void create_ShouldSaveOutMovement_WhenStockIsSufficient() {
    StockRequest request =
        StockRequest.builder()
            .bookFormatId(bookFormatId)
            .movement(MovementType.OUT)
            .quantity(4)
            .build();

    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));
    when(bookFormatRepository.existsById(bookFormatId)).thenReturn(true);
    when(stockRepository.findByBookFormatId(bookFormatId)).thenReturn(List.of(stockMovementIn));
    when(stockRepository.save(any(StockMovement.class))).thenReturn(stockMovementOut);

    StockResponse result = stockService.create(request);

    assertNotNull(result);
    assertEquals(MovementType.OUT, result.getMovement());
    verify(bookFormatRepository, times(1)).findById(bookFormatId);
    verify(stockRepository, times(1)).save(any(StockMovement.class));
  }

  @Test
  void create_ShouldThrowException_WhenBookFormatNotFound() {
    StockRequest request =
        StockRequest.builder()
            .bookFormatId(bookFormatId)
            .movement(MovementType.IN)
            .quantity(5)
            .build();

    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> stockService.create(request));

    assertEquals("BookFormat not found with id: " + bookFormatId, exception.getMessage());
    verify(bookFormatRepository, times(1)).findById(bookFormatId);
    verify(stockRepository, never()).save(any(StockMovement.class));
  }

  @Test
  void create_ShouldThrowException_WhenStockIsInsufficientForOutMovement() {
    StockRequest request =
        StockRequest.builder()
            .bookFormatId(bookFormatId)
            .movement(MovementType.OUT)
            .quantity(100)
            .build();

    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));
    when(bookFormatRepository.existsById(bookFormatId)).thenReturn(true);
    when(stockRepository.findByBookFormatId(bookFormatId)).thenReturn(List.of(stockMovementIn));

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> stockService.create(request));

    assertTrue(exception.getMessage().contains("Stock insuffisant"));
    verify(stockRepository, never()).save(any(StockMovement.class));
  }

  @Test
  void getStockSummary_ShouldAggregateStockByFormatType() {
    Book book2 = Book.builder().id(UUID.randomUUID()).title("Second Book").build();
    BookFormat bookFormat2 =
        BookFormat.builder().id(UUID.randomUUID()).book(book2).format(format).price(9.99).build();

    when(bookFormatRepository.findAll()).thenReturn(List.of(bookFormat, bookFormat2));
    when(bookFormatRepository.existsById(bookFormat.getId())).thenReturn(true);
    when(bookFormatRepository.existsById(bookFormat2.getId())).thenReturn(true);
    when(stockRepository.findByBookFormatId(bookFormat.getId()))
        .thenReturn(List.of(stockMovementIn, stockMovementOut));
    when(stockRepository.findByBookFormatId(bookFormat2.getId()))
        .thenReturn(List.of(stockMovementIn));

    Map<String, Integer> result = stockService.getStockSummary();

    assertNotNull(result);
    assertEquals(16, result.get("Paperback"));
    verify(bookFormatRepository, times(1)).findAll();
  }

  @Test
  void getStockSummary_ShouldReturnEmptyMap_WhenNoBookFormats() {
    when(bookFormatRepository.findAll()).thenReturn(List.of());

    Map<String, Integer> result = stockService.getStockSummary();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(bookFormatRepository, times(1)).findAll();
  }
}
