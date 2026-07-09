package com.example.demo.librairie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.librairie.dto.OrderLineRequest;
import com.example.demo.librairie.dto.OrderRequest;
import com.example.demo.librairie.dto.OrderResponse;
import com.example.demo.librairie.entity.*;
import com.example.demo.librairie.exception.ResourceNotFoundException;
import com.example.demo.librairie.repository.BookFormatRepository;
import com.example.demo.librairie.repository.CustomerRepository;
import com.example.demo.librairie.repository.OrderRepository;
import com.example.demo.librairie.repository.StockRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
class OrderServiceTest {

  @Mock private OrderRepository orderRepository;

  @Mock private CustomerRepository customerRepository;

  @Mock private BookFormatRepository bookFormatRepository;
  @Mock private StockRepository stockRepository;
  @Mock private StockService stockService;

  @InjectMocks private OrderService orderService;

  private UUID orderId;
  private UUID customerId;
  private UUID bookFormatId;
  private Customer customer;
  private BookFormat bookFormat;
  private Order order;
  private OrderRequest orderRequest;

  @BeforeEach
  void setUp() {
    orderId = UUID.randomUUID();
    customerId = UUID.randomUUID();
    bookFormatId = UUID.randomUUID();

    customer =
        Customer.builder()
            .id(customerId)
            .name("Doe")
            .firstname("John")
            .email("john.doe@example.com")
            .build();

    Book book = Book.builder().id(UUID.randomUUID()).title("Les Misérables").build();
    Format format = Format.builder().id(UUID.randomUUID()).formatType("EBOOK").build();
    bookFormat =
        BookFormat.builder().id(bookFormatId).price(19.90).book(book).format(format).build();

    OrderLine line =
        OrderLine.builder()
            .id(UUID.randomUUID())
            .bookFormat(bookFormat)
            .quantity(2)
            .unitPrice(19.90)
            .build();

    List<OrderLine> lines = new ArrayList<>();
    lines.add(line);

    order =
        Order.builder()
            .id(orderId)
            .orderType(OrderType.DELIVERY)
            .orderDate(LocalDateTime.of(2026, 7, 1, 10, 0))
            .customer(customer)
            .lines(lines)
            .build();
    line.setOrder(order);

    orderRequest =
        OrderRequest.builder()
            .customerId(customerId)
            .orderType(OrderType.DELIVERY)
            .orderDate(LocalDateTime.of(2026, 7, 1, 10, 0))
            .lines(
                List.of(OrderLineRequest.builder().bookFormatId(bookFormatId).quantity(2).build()))
            .build();
  }

  @Test
  void getAllOrders_ShouldReturnListOfOrderResponses() {
    when(orderRepository.findAll()).thenReturn(List.of(order));

    List<OrderResponse> result = orderService.getAllOrders();

    assertEquals(1, result.size());
    assertEquals(orderId, result.get(0).getId());
    assertEquals(39.80, result.get(0).getTotalAmount());
    verify(orderRepository, times(1)).findAll();
  }

  @Test
  void getOrderById_ShouldReturnOrderResponse_WhenFound() {
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    OrderResponse response = orderService.getOrderById(orderId);

    assertNotNull(response);
    assertEquals(orderId, response.getId());
    assertEquals(customerId, response.getCustomerId());
    assertEquals("John Doe", response.getCustomerFullName());
    assertEquals(1, response.getLines().size());
    assertEquals("Les Misérables", response.getLines().get(0).getBookTitle());
    assertEquals("EBOOK", response.getLines().get(0).getFormatType());
    assertEquals(39.80, response.getLines().get(0).getTotalPrice());
  }

  @Test
  void getOrderById_ShouldThrowException_WhenNotFound() {
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(orderId));
  }

  @Test
  void getOrdersByCustomer_ShouldReturnOrders_WhenCustomerExists() {
    when(customerRepository.existsById(customerId)).thenReturn(true);
    when(orderRepository.findByCustomerId(customerId)).thenReturn(List.of(order));

    List<OrderResponse> result = orderService.getOrdersByCustomer(customerId);

    assertEquals(1, result.size());
    verify(orderRepository, times(1)).findByCustomerId(customerId);
  }

  @Test
  void getOrdersByCustomer_ShouldThrowException_WhenCustomerNotFound() {
    when(customerRepository.existsById(customerId)).thenReturn(false);

    assertThrows(
        ResourceNotFoundException.class, () -> orderService.getOrdersByCustomer(customerId));
    verify(orderRepository, never()).findByCustomerId(any());
  }

  @Test
  void createOrder_ShouldReturnOrderResponse_WhenSuccessful() {
    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));

    when(stockService.getCurrentStock(bookFormatId)).thenReturn(10);

    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse response = orderService.createOrder(orderRequest);

    assertNotNull(response);
    assertEquals(orderId, response.getId());
    assertEquals(OrderType.DELIVERY, response.getOrderType());
    assertEquals(1, response.getLines().size());
    verify(orderRepository, times(1)).save(any(Order.class));
  }

  @Test
  void createOrder_ShouldDefaultOrderDate_WhenNotProvided() {
    orderRequest.setOrderDate(null);
    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));

    when(stockService.getCurrentStock(bookFormatId)).thenReturn(10);

    when(orderRepository.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    OrderResponse response = orderService.createOrder(orderRequest);

    assertNotNull(response.getOrderDate());
  }

  @Test
  void createOrder_ShouldThrowException_WhenCustomerNotFound() {
    when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(orderRequest));
    verify(orderRepository, never()).save(any());
  }

  @Test
  void createOrder_ShouldThrowException_WhenBookFormatNotFound() {
    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(orderRequest));
    verify(orderRepository, never()).save(any());
  }

  @Test
  void updateOrder_ShouldReturnUpdatedOrderResponse_WhenFound() {
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));
    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse response = orderService.updateOrder(orderId, orderRequest);

    assertNotNull(response);
    assertEquals(orderId, response.getId());
    verify(orderRepository, times(1)).save(any(Order.class));
  }

  @Test
  void updateOrder_ShouldThrowException_WhenOrderNotFound() {
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> orderService.updateOrder(orderId, orderRequest));
    verify(orderRepository, never()).save(any());
  }

  @Test
  void updateOrder_ShouldThrowException_WhenCustomerNotFound() {
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> orderService.updateOrder(orderId, orderRequest));
    verify(orderRepository, never()).save(any());
  }

  @Test
  void deleteOrder_ShouldDeleteOrder_WhenFound() {
    when(orderRepository.existsById(orderId)).thenReturn(true);
    doNothing().when(orderRepository).deleteById(orderId);

    orderService.deleteOrder(orderId);

    verify(orderRepository, times(1)).deleteById(orderId);
  }

  @Test
  void deleteOrder_ShouldThrowException_WhenNotFound() {
    when(orderRepository.existsById(orderId)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrder(orderId));
    verify(orderRepository, never()).deleteById(any());
  }

  @Test
  void createOrder_ShouldThrowException_WhenStockIsInsufficient() {
    int currentStock = 1;
    orderRequest.getLines().get(0).setQuantity(5);

    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));
    when(stockService.getCurrentStock(bookFormatId)).thenReturn(currentStock);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(orderRequest));

    assertTrue(exception.getMessage().contains("Insufficient stock"));
    verify(stockRepository, never()).save(any());
    verify(orderRepository, never()).save(any());
  }

  @Test
  void createOrder_ShouldSaveStockMovement_WhenStockIsSufficient() {
    int currentStock = 10;
    orderRequest.getLines().get(0).setQuantity(2);

    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));
    when(stockService.getCurrentStock(bookFormatId)).thenReturn(currentStock);
    when(orderRepository.save(any(Order.class))).thenReturn(order);

    OrderResponse response = orderService.createOrder(orderRequest);

    assertNotNull(response);
    verify(stockRepository, times(1)).save(any(StockMovement.class));
    verify(orderRepository, times(1)).save(any(Order.class));
  }

  @Test
  void getOrderById_ShouldReturnResponseWithEmptyLines_WhenOrderHasNoLines() {
    Order orderWithNoLines =
        Order.builder()
            .id(orderId)
            .orderType(OrderType.DELIVERY)
            .orderDate(LocalDateTime.of(2026, 7, 1, 10, 0))
            .customer(customer)
            .lines(null)
            .build();

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderWithNoLines));

    OrderResponse response = orderService.getOrderById(orderId);

    assertNotNull(response);
    assertTrue(response.getLines().isEmpty());
    assertEquals(0.0, response.getTotalAmount());
  }

  @Test
  void updateOrder_ShouldThrowException_WhenBookFormatNotFound() {
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> orderService.updateOrder(orderId, orderRequest));
    verify(orderRepository, never()).save(any());
  }

  @Test
  void updateOrder_ShouldDefaultOrderDate_WhenNotProvidedInRequest() {
    orderRequest.setOrderDate(null);

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
    when(bookFormatRepository.findById(bookFormatId)).thenReturn(Optional.of(bookFormat));
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    OrderResponse response = orderService.updateOrder(orderId, orderRequest);

    assertNotNull(response.getOrderDate());
    assertEquals(order.getOrderDate(), response.getOrderDate());
  }
}
