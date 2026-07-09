package com.example.demo.librairie.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.librairie.dto.OrderLineRequest;
import com.example.demo.librairie.dto.OrderLineResponse;
import com.example.demo.librairie.dto.OrderRequest;
import com.example.demo.librairie.dto.OrderResponse;
import com.example.demo.librairie.entity.OrderType;
import com.example.demo.librairie.exception.GlobalExceptionHandler;
import com.example.demo.librairie.exception.ResourceNotFoundException;
import com.example.demo.librairie.service.OrderService;
// IMPORTANT : Ajustez cet import vers le package exact de votre @ControllerAdvice global
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Mock private OrderService orderService;

  @InjectMocks private OrderController orderController;

  private UUID orderId;
  private UUID customerId;
  private UUID bookFormatId;
  private OrderRequest orderRequest;
  private OrderResponse orderResponse;

  @BeforeEach
  void setUp() {
    this.mockMvc =
        MockMvcBuilders.standaloneSetup(orderController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    orderId = UUID.randomUUID();
    customerId = UUID.randomUUID();
    bookFormatId = UUID.randomUUID();

    orderRequest =
        OrderRequest.builder()
            .customerId(customerId)
            .orderType(OrderType.DELIVERY)
            .orderDate(LocalDateTime.of(2026, 7, 1, 10, 0))
            .lines(
                List.of(OrderLineRequest.builder().bookFormatId(bookFormatId).quantity(2).build()))
            .build();

    OrderLineResponse lineResponse =
        OrderLineResponse.builder()
            .id(UUID.randomUUID())
            .bookFormatId(bookFormatId)
            .bookTitle("Les Misérables")
            .formatType("EBOOK")
            .quantity(2)
            .unitPrice(19.90)
            .totalPrice(39.80)
            .build();

    orderResponse =
        OrderResponse.builder()
            .id(orderId)
            .orderType(OrderType.DELIVERY)
            .orderDate(LocalDateTime.of(2026, 7, 1, 10, 0))
            .customerId(customerId)
            .customerFullName("John Doe")
            .lines(List.of(lineResponse))
            .totalAmount(39.80)
            .build();
  }

  @Test
  void getAll_ShouldReturnListOfOrders() throws Exception {
    when(orderService.getAllOrders()).thenReturn(List.of(orderResponse));

    mockMvc
        .perform(get("/orders").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(orderId.toString()))
        .andExpect(jsonPath("$[0].customerFullName").value("John Doe"))
        .andExpect(jsonPath("$[0].totalAmount").value(39.80));

    verify(orderService).getAllOrders();
  }

  @Test
  void getById_WithValidId_ShouldReturnOrder() throws Exception {
    when(orderService.getOrderById(orderId)).thenReturn(orderResponse);

    mockMvc
        .perform(get("/orders/{id}", orderId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderId.toString()))
        .andExpect(jsonPath("$.orderType").value("DELIVERY"))
        .andExpect(jsonPath("$.lines", hasSize(1)))
        .andExpect(jsonPath("$.lines[0].bookTitle").value("Les Misérables"));

    verify(orderService).getOrderById(orderId);
  }

  @Test
  void getById_WithInvalidId_ShouldReturnNotFound() throws Exception {
    UUID invalidId = UUID.randomUUID();
    when(orderService.getOrderById(invalidId))
        .thenThrow(new ResourceNotFoundException("Order", invalidId));

    mockMvc
        .perform(get("/orders/{id}", invalidId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    verify(orderService).getOrderById(invalidId);
  }

  @Test
  void getByCustomer_ShouldReturnOrders() throws Exception {
    when(orderService.getOrdersByCustomer(customerId)).thenReturn(List.of(orderResponse));

    mockMvc
        .perform(get("/orders/customer/{customerId}", customerId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].customerId").value(customerId.toString()));

    verify(orderService).getOrdersByCustomer(customerId);
  }

  @Test
  void getByCustomer_WithInvalidCustomer_ShouldReturnNotFound() throws Exception {
    UUID invalidCustomerId = UUID.randomUUID();
    when(orderService.getOrdersByCustomer(invalidCustomerId))
        .thenThrow(new ResourceNotFoundException("Customer", invalidCustomerId));

    mockMvc
        .perform(get("/orders/customer/{customerId}", invalidCustomerId))
        .andExpect(status().isNotFound());

    verify(orderService).getOrdersByCustomer(invalidCustomerId);
  }

  @Test
  void create_WithValidRequest_ShouldReturnCreatedOrder() throws Exception {
    when(orderService.createOrder(any(OrderRequest.class))).thenReturn(orderResponse);

    mockMvc
        .perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(orderId.toString()))
        .andExpect(jsonPath("$.totalAmount").value(39.80));

    verify(orderService).createOrder(any(OrderRequest.class));
  }

  @Test
  void create_WithMissingCustomerId_ShouldReturnBadRequest() throws Exception {
    OrderRequest invalidRequest =
        OrderRequest.builder()
            .orderType(OrderType.DELIVERY)
            .lines(
                List.of(OrderLineRequest.builder().bookFormatId(bookFormatId).quantity(2).build()))
            .build();

    mockMvc
        .perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void create_WithEmptyLines_ShouldReturnBadRequest() throws Exception {
    OrderRequest invalidRequest =
        OrderRequest.builder()
            .customerId(customerId)
            .orderType(OrderType.DELIVERY)
            .lines(List.of())
            .build();

    mockMvc
        .perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void create_WithUnknownCustomer_ShouldReturnNotFound() throws Exception {
    when(orderService.createOrder(any(OrderRequest.class)))
        .thenThrow(new ResourceNotFoundException("Customer", customerId));

    mockMvc
        .perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
        .andExpect(status().isNotFound());

    verify(orderService).createOrder(any(OrderRequest.class));
  }

  @Test
  void update_WithValidIdAndRequest_ShouldReturnUpdatedOrder() throws Exception {
    when(orderService.updateOrder(eq(orderId), any(OrderRequest.class))).thenReturn(orderResponse);

    mockMvc
        .perform(
            put("/orders/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderId.toString()));

    verify(orderService).updateOrder(eq(orderId), any(OrderRequest.class));
  }

  @Test
  void update_WithInvalidId_ShouldReturnNotFound() throws Exception {
    UUID invalidId = UUID.randomUUID();
    when(orderService.updateOrder(eq(invalidId), any(OrderRequest.class)))
        .thenThrow(new ResourceNotFoundException("Order", invalidId));

    mockMvc
        .perform(
            put("/orders/{id}", invalidId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
        .andExpect(status().isNotFound());

    verify(orderService).updateOrder(eq(invalidId), any(OrderRequest.class));
  }

  @Test
  void delete_WithValidId_ShouldReturnNoContent() throws Exception {
    doNothing().when(orderService).deleteOrder(orderId);

    mockMvc.perform(delete("/orders/{id}", orderId)).andExpect(status().isNoContent());

    verify(orderService).deleteOrder(orderId);
  }

  @Test
  void delete_WithInvalidId_ShouldReturnNotFound() throws Exception {
    UUID invalidId = UUID.randomUUID();
    doThrow(new ResourceNotFoundException("Order", invalidId))
        .when(orderService)
        .deleteOrder(invalidId);

    mockMvc.perform(delete("/orders/{id}", invalidId)).andExpect(status().isNotFound());

    verify(orderService).deleteOrder(invalidId);
  }
}
