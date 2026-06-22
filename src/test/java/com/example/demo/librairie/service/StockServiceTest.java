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

  private UUID bookFormatId;
  private UUID stockId1;
  private UUID stockId2;
  private BookFormat bookFormat;
  private StockMovement stockIn;
  private StockMovement stockOut;
  private StockRequest stockRequest;

  @BeforeEach
  void setUp() {
    bookFormatId = UUID.randomUUID();
    stockId1 = UUID.randomUUID();
    stockId2 = UUID.randomUUID();

    Book book = Book.builder().id(UUID.randomUUID()).title("Les Misérables").build();

    Format format = Format.builder().id(UUID.randomUUID()).formatType("Poche").build();

    bookFormat =
        BookFormat.builder().id(bookFormatId).book(book).format(format).price(12.99).build();

    stockIn =
        StockMovement.builder()
            .id(stockId1)
            .movement(MovementType.IN)
            .quantity(50)
            .movementDate(LocalDateTime.now())
            .bookFormat(bookFormat)
            .build();

    stockOut =
        StockMovement.builder()
            .id(stockId2)
            .movement(MovementType.OUT)
            .quantity(12)
            .movementDate(LocalDateTime.now())
            .bookFormat(bookFormat)
            .build();

    stockRequest =
        StockRequest.builder()
            .bookFormatId(bookFormatId)
            .movement(MovementType.IN)
            .quantity(20)
            .build();
  }

  @Test
  void getAll_ShouldReturnListOfStocks_WhenStocksExist() {
    when(stockRepository.findAll()).thenReturn(List.of(stockIn, stockOut));

    List<StockResponse> result = stockService.getAll();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(stockId1, result.get(0).getId());
    assertEquals(MovementType.IN, result.get(0).getMovement());
    assertEquals(50, result.get(0).getQuantity());
    assertEquals(stockId2, result.get(1).getId());
    assertEquals(MovementType.OUT, result.get(1).getMovement());
    assertEquals(12, result.get(1).getQuantity());
    assertEquals(bookFormatId, result.get(0).getBookFormatId());

    verify(stockRepository, times(1)).findAll();
  }

  @Test
  void getAll_ShouldReturnEmptyList_WhenNoStocksExist() {
    when(stockRepository.findAll()).thenReturn(List.of());

    List<StockResponse> result = stockService.getAll();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(stockRepository, times(1)).findAll();
  }

  @Test
  void getById_ShouldReturnStock_WhenExists() {
    when(stockRepository.findById(stockId1)).thenReturn(Optional.of(stockIn));

    StockResponse result = stockService.getById(stockId1);

    assertNotNull(result);
    assertEquals(stockId1, result.getId());
    assertEquals(MovementType.IN, result.getMovement());
    assertEquals(50, result.getQuantity());
    assertEquals(bookFormatId, result.getBookFormatId());

    verify(stockRepository, times(1)).findById(stockId1);
  }

  @Test
  void getById_ShouldThrowException_WhenNotFound() {
    when(stockRepository.findById(stockId1)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> stockService.getById(stockId1));

    assertEquals("Stock not found with id: " + stockId1, exception.getMessage());
    verify(stockRepository, times(1)).findById(stockId1);
  }

  @Test
  void getByBookFormatId_ShouldReturnStocks_WhenBookFormatExists() {
    when(bookFormatRepository.existsById(bookFormatId)).thenReturn(true);
    when(stockRepository.findByBookFormatId(bookFormatId)).thenReturn(List.of(stockIn, stockOut));

    List<StockResponse> result = stockService.getByBookFormatId(bookFormatId);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(bookFormatId, result.get(0).getBookFormatId());
    assertEquals(bookFormatId, result.get(1).getBookFormatId());

    verify(bookFormatRepository, times(1)).existsById(bookFormatId);
    verify(stockRepository, times(1)).findByBookFormatId(bookFormatId);
  }

  @Test
  void getByBookFormatId_ShouldReturnEmptyList_WhenNoStocksForBookFormat() {
    when(bookFormatRepository.existsById(bookFormatId)).thenReturn(true);
    when(stockRepository.findByBookFormatId(bookFormatId)).thenReturn(List.of());

    List<StockResponse> result = stockService.getByBookFormatId(bookFormatId);

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(bookFormatRepository, times(1)).existsById(bookFormatId);
    verify(stockRepository, times(1)).findByBookFormatId(bookFormatId);
  }

  @Test
  void getByBookFormatId_ShouldThrowException_WhenBookFormatNotFound() {
    UUID invalidId = UUID.randomUUID();
    when(bookFormatRepository.existsById(invalidId)).thenReturn(false);

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> stockService.getByBookFormatId(invalidId));

    assertEquals("BookFormat not found with id: " + invalidId, exception.getMessage());
    verify(bookFormatRepository, times(1)).existsById(invalidId);
    verify(stockRepository, never()).findByBookFormatId(any());
  }

  @Test
  void getCurrentStock_ShouldReturnCorrectStock_WhenMouvementsExist() {
    when(bookFormatRepository.existsById(bookFormatId)).thenReturn(true);
    when(stockRepository.findByBookFormatId(bookFormatId)).thenReturn(List.of(stockIn, stockOut));

    Integer result = stockService.getCurrentStock(bookFormatId);

    assertNotNull(result);
    assertEquals(38, result);

    verify(bookFormatRepository, times(1)).existsById(bookFormatId);
    verify(stockRepository, times(1)).findByBookFormatId(bookFormatId);
  }

  @Test
  void getCurrentStock_ShouldReturnZero_WhenNoMouvementsExist() {
    when(bookFormatRepository.existsById(bookFormatId)).thenReturn(true);
    when(stockRepository.findByBookFormatId(bookFormatId)).thenReturn(List.of());

    Integer result = stockService.getCurrentStock(bookFormatId);

    assertNotNull(result);
    assertEquals(0, result);

    verify(bookFormatRepository, times(1)).existsById(bookFormatId);
    verify(stockRepository, times(1)).findByBookFormatId(bookFormatId);
  }

  @Test
  void getCurrentStock_ShouldThrowException_WhenBookFormatNotFound() {
    UUID invalidId = UUID.randomUUID();
    when(bookFormatRepository.existsById(invalidId)).thenReturn(false);

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> stockService.getCurrentStock(invalidId));

    assertEquals("BookFormat not found with id: " + invalidId, exception.getMessage());
    verify(bookFormatRepository, times(1)).existsById(invalidId);
    verify(stockRepository, never()).findByBookFormatId(any());
  }

  @Test
  void create_ShouldReturnCreatedStock_WhenMovementIsIN() {
    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));

    StockMovement savedStock =
        StockMovement.builder()
            .id(UUID.randomUUID())
            .movement(stockRequest.getMovement())
            .quantity(stockRequest.getQuantity())
            .movementDate(LocalDateTime.now())
            .bookFormat(bookFormat)
            .build();

    when(stockRepository.save(any(StockMovement.class))).thenReturn(savedStock);

    StockResponse result = stockService.create(stockRequest);

    assertNotNull(result);
    assertEquals(MovementType.IN, result.getMovement());
    assertEquals(20, result.getQuantity());
    assertEquals(bookFormatId, result.getBookFormatId());

    verify(bookFormatRepository, times(1)).findById(bookFormatId);
    verify(stockRepository, times(1)).save(any(StockMovement.class));
  }

  @Test
  void create_ShouldReturnCreatedStock_WhenMovementIsOUT_WithSufficientStock() {
    StockRequest outRequest =
        StockRequest.builder()
            .bookFormatId(bookFormatId)
            .movement(MovementType.OUT)
            .quantity(10)
            .build();

    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));
    when(bookFormatRepository.existsById(bookFormatId)).thenReturn(true);
    when(stockRepository.findByBookFormatId(bookFormatId)).thenReturn(List.of(stockIn, stockOut));

    StockMovement savedStock =
        StockMovement.builder()
            .id(UUID.randomUUID())
            .movement(MovementType.OUT)
            .quantity(10)
            .movementDate(LocalDateTime.now())
            .bookFormat(bookFormat)
            .build();

    when(stockRepository.save(any(StockMovement.class))).thenReturn(savedStock);

    StockResponse result = stockService.create(outRequest);

    assertNotNull(result);
    assertEquals(MovementType.OUT, result.getMovement());
    assertEquals(10, result.getQuantity());

    verify(bookFormatRepository, times(1)).findById(bookFormatId);
    verify(stockRepository, times(1)).save(any(StockMovement.class));
  }

  @Test
  void create_ShouldThrowException_WhenMovementIsOUT_WithInsufficientStock() {
    StockRequest outRequest =
        StockRequest.builder()
            .bookFormatId(bookFormatId)
            .movement(MovementType.OUT)
            .quantity(100)
            .build();

    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));
    when(bookFormatRepository.existsById(bookFormatId)).thenReturn(true);
    when(stockRepository.findByBookFormatId(bookFormatId)).thenReturn(List.of(stockIn, stockOut));

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> stockService.create(outRequest));

    assertTrue(
        exception.getMessage().contains("Stock insuffisant ! Stock actuel : 38, demandé : 100"));

    verify(bookFormatRepository, times(1)).findById(bookFormatId);
    verify(stockRepository, never()).save(any(StockMovement.class));
  }

  @Test
  void create_ShouldThrowException_WhenBookFormatNotFound() {
    UUID invalidId = UUID.randomUUID();
    StockRequest invalidRequest =
        StockRequest.builder()
            .bookFormatId(invalidId)
            .movement(MovementType.IN)
            .quantity(10)
            .build();

    when(bookFormatRepository.findById(invalidId)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> stockService.create(invalidRequest));

    assertEquals("BookFormat not found with id: " + invalidId, exception.getMessage());

    verify(bookFormatRepository, times(1)).findById(invalidId);
    verify(stockRepository, never()).save(any(StockMovement.class));
  }

  @Test
  void getStockSummary_ShouldReturnMap_WhenBookFormatsExist() {
    when(bookFormatRepository.findAll()).thenReturn(List.of(bookFormat));
    when(bookFormatRepository.existsById(bookFormatId)).thenReturn(true);
    when(stockRepository.findByBookFormatId(bookFormatId)).thenReturn(List.of(stockIn, stockOut));

    Map<String, Integer> result = stockService.getStockSummary();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.containsKey("Poche"));
    assertEquals(38, result.get("Poche"));
  }

  @Test
  void getStockSummary_ShouldReturnEmptyMap_WhenNoBookFormatsExist() {
    when(bookFormatRepository.findAll()).thenReturn(List.of());

    Map<String, Integer> result = stockService.getStockSummary();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
