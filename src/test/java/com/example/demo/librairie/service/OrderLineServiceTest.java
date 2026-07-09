package com.example.demo.librairie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.librairie.dto.OrderLineRequest;
import com.example.demo.librairie.dto.OrderLineResponse;
import com.example.demo.librairie.entity.Book;
import com.example.demo.librairie.entity.BookFormat;
import com.example.demo.librairie.entity.Format;
import com.example.demo.librairie.entity.Order;
import com.example.demo.librairie.entity.OrderLine;
import com.example.demo.librairie.repository.BookFormatRepository;
import com.example.demo.librairie.repository.OrderLineRepository;
import com.example.demo.librairie.repository.OrderRepository;
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
class OrderLineServiceTest {

  @Mock private OrderLineRepository orderLineRepository;

  @Mock private OrderRepository orderRepository;

  @Mock private BookFormatRepository bookFormatRepository;

  @InjectMocks private OrderLineService orderLineService;

  private UUID orderLineId;
  private UUID orderId;
  private UUID bookFormatId;

  private Order order;
  private BookFormat bookFormat;
  private OrderLine orderLine;
  private OrderLineRequest orderLineRequest;

  @BeforeEach
  void setUp() {

    orderLineId = UUID.randomUUID();
    orderId = UUID.randomUUID();
    bookFormatId = UUID.randomUUID();

    Book book = Book.builder().id(UUID.randomUUID()).title("Les Misérables").build();

    Format format = Format.builder().id(UUID.randomUUID()).formatType("Poche").build();

    bookFormat =
        BookFormat.builder().id(bookFormatId).price(12.99).book(book).format(format).build();

    order = Order.builder().id(orderId).build();

    orderLine =
        OrderLine.builder()
            .id(orderLineId)
            .order(order)
            .bookFormat(bookFormat)
            .quantity(2)
            .unitPrice(12.99)
            .build();

    orderLineRequest =
        OrderLineRequest.builder().orderId(orderId).bookFormatId(bookFormatId).quantity(2).build();
  }

  @Test
  void getAll_ShouldReturnListOfOrderLines_WhenOrderLinesExist() {

    when(orderLineRepository.findAll()).thenReturn(List.of(orderLine));

    List<OrderLineResponse> result = orderLineService.getAll();

    assertNotNull(result);
    assertEquals(1, result.size());

    assertEquals(orderLineId, result.get(0).getId());
    assertEquals(orderId, result.get(0).getOrderId());
    assertEquals(bookFormatId, result.get(0).getBookFormatId());

    assertEquals("Les Misérables", result.get(0).getBookTitle());
    assertEquals("Poche", result.get(0).getFormatType());

    assertEquals(2, result.get(0).getQuantity());
    assertEquals(12.99, result.get(0).getUnitPrice());

    verify(orderLineRepository, times(1)).findAll();
  }

  @Test
  void getAll_ShouldReturnEmptyList_WhenNoOrderLinesExist() {

    when(orderLineRepository.findAll()).thenReturn(List.of());

    List<OrderLineResponse> result = orderLineService.getAll();

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(orderLineRepository, times(1)).findAll();
  }

  @Test
  void getById_ShouldReturnOrderLine_WhenExists() {

    when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));

    OrderLineResponse result = orderLineService.getById(orderLineId);

    assertNotNull(result);

    assertEquals(orderLineId, result.getId());
    assertEquals(orderId, result.getOrderId());
    assertEquals(bookFormatId, result.getBookFormatId());
    assertEquals(2, result.getQuantity());

    verify(orderLineRepository, times(1)).findById(orderLineId);
  }

  @Test
  void getById_ShouldThrowException_WhenNotFound() {

    when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> orderLineService.getById(orderLineId));

    assertEquals("OrderLine not found with id: " + orderLineId, exception.getMessage());

    verify(orderLineRepository, times(1)).findById(orderLineId);
  }

  @Test
  void create_ShouldCreateOrderLine_WithBookFormatPrice() {

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));

    when(orderLineRepository.save(any(OrderLine.class))).thenReturn(orderLine);

    OrderLineResponse result = orderLineService.create(orderLineRequest);

    assertNotNull(result);

    assertEquals(orderId, result.getOrderId());
    assertEquals(bookFormatId, result.getBookFormatId());

    assertEquals(12.99, result.getUnitPrice());
    assertEquals(2, result.getQuantity());

    verify(orderRepository, times(1)).findById(orderId);

    verify(bookFormatRepository, times(1)).findById(bookFormatId);

    verify(orderLineRepository, times(1)).save(any(OrderLine.class));
  }

  @Test
  void create_ShouldThrowException_WhenOrderNotFound() {

    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> orderLineService.create(orderLineRequest));

    assertTrue(exception.getMessage().contains("Order not found"));

    verify(orderLineRepository, never()).save(any(OrderLine.class));
  }

  @Test
  void create_ShouldThrowException_WhenBookFormatNotFound() {

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> orderLineService.create(orderLineRequest));

    assertTrue(exception.getMessage().contains("BookFormat not found"));

    verify(orderLineRepository, never()).save(any(OrderLine.class));
  }

  @Test
  void update_ShouldUpdateOrderLine_WhenExists() {

    when(orderLineRepository.findById(orderLineId)).thenReturn(Optional.of(orderLine));

    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));

    when(orderLineRepository.save(any(OrderLine.class))).thenReturn(orderLine);

    OrderLineResponse result = orderLineService.update(orderLineId, orderLineRequest);

    assertNotNull(result);
    assertEquals(2, result.getQuantity());
    assertEquals(12.99, result.getUnitPrice());

    verify(orderLineRepository, times(1)).findById(orderLineId);

    verify(orderLineRepository, times(1)).save(any(OrderLine.class));
  }

  @Test
  void delete_ShouldDeleteOrderLine_WhenExists() {

    when(orderLineRepository.existsById(orderLineId)).thenReturn(true);

    orderLineService.delete(orderLineId);

    verify(orderLineRepository, times(1)).existsById(orderLineId);

    verify(orderLineRepository, times(1)).deleteById(orderLineId);
  }

  @Test
  void delete_ShouldThrowException_WhenNotFound() {

    when(orderLineRepository.existsById(orderLineId)).thenReturn(false);

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> orderLineService.delete(orderLineId));

    assertEquals("OrderLine not found with id: " + orderLineId, exception.getMessage());

    verify(orderLineRepository, never()).deleteById(orderLineId);
  }
}
