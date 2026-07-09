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
import com.example.demo.librairie.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private OrderService orderService;

  private UUID orderId;
  private UUID customerId;
  private UUID bookFormatId;

  private OrderRequest orderRequest;

  private OrderResponse orderResponse;

  private List<OrderResponse> orderResponseList;

  @BeforeEach
  void setUp() {

    orderId = UUID.randomUUID();
    customerId = UUID.randomUUID();
    bookFormatId = UUID.randomUUID();

    OrderLineResponse line =
        OrderLineResponse.builder()
            .id(UUID.randomUUID())
            .orderId(orderId)
            .bookFormatId(bookFormatId)
            .bookTitle("Les Misérables")
            .formatType("Poche")
            .quantity(2)
            .unitPrice(19.90)
            .totalPrice(39.80)
            .build();

    orderRequest =
        OrderRequest.builder()
            .customerId(customerId)
            .orderType(OrderType.DELIVERY)
            .orderDate(LocalDateTime.of(2026, 7, 1, 10, 0))
            .lines(
                List.of(
                    OrderLineRequest.builder()
                        .orderId(orderId)
                        .bookFormatId(bookFormatId)
                        .quantity(2)
                        .build()))
            .build();

    orderResponse =
        OrderResponse.builder()
            .id(orderId)
            .customerId(customerId)
            .customerFullName("John Doe")
            .orderType(OrderType.DELIVERY)
            .orderDate(LocalDateTime.of(2026, 7, 1, 10, 0))
            .lines(List.of(line))
            .totalAmount(39.80)
            .build();

    OrderResponse orderResponse2 =
        OrderResponse.builder()
            .id(UUID.randomUUID())
            .customerId(customerId)
            .customerFullName("Jane Doe")
            .orderType(OrderType.DELIVERY)
            .orderDate(LocalDateTime.of(2026, 7, 2, 10, 0))
            .lines(List.of())
            .totalAmount(20.00)
            .build();

    orderResponseList = List.of(orderResponse, orderResponse2);
  }

  @Test
  void getAll_ShouldReturnListOfOrders() throws Exception {

    when(orderService.getAllOrders()).thenReturn(orderResponseList);

    mockMvc
        .perform(get("/orders").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
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
        .andExpect(jsonPath("$.customerFullName").value("John Doe"))
        .andExpect(jsonPath("$.totalAmount").value(39.80));

    verify(orderService).getOrderById(orderId);
  }

  @Test
  void getById_WithInvalidId_ShouldReturnServerError() throws Exception {

    UUID invalidId = UUID.randomUUID();

    when(orderService.getOrderById(invalidId))
        .thenThrow(new RuntimeException("Order not found with id: " + invalidId));

    mockMvc
        .perform(get("/orders/{id}", invalidId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is5xxServerError());

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
  void create_WithValidRequest_ShouldReturnCreatedOrder() throws Exception {

    when(orderService.createOrder(any(OrderRequest.class))).thenReturn(orderResponse);

    mockMvc
        .perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper
                        .registerModule(new JavaTimeModule())
                        .writeValueAsString(orderRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(orderId.toString()))
        .andExpect(jsonPath("$.totalAmount").value(39.80));

    verify(orderService).createOrder(any(OrderRequest.class));
  }

  @Test
  void create_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {

    OrderRequest invalidRequest =
        OrderRequest.builder()
            .customerId(null)
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
  void update_WithInvalidId_ShouldReturnServerError() throws Exception {

    UUID invalidId = UUID.randomUUID();

    when(orderService.updateOrder(eq(invalidId), any(OrderRequest.class)))
        .thenThrow(new RuntimeException("Order not found with id: " + invalidId));

    mockMvc
        .perform(
            put("/orders/{id}", invalidId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
        .andExpect(status().is5xxServerError());

    verify(orderService).updateOrder(eq(invalidId), any(OrderRequest.class));
  }

  @Test
  void delete_WithValidId_ShouldReturnNoContent() throws Exception {

    doNothing().when(orderService).deleteOrder(orderId);

    mockMvc.perform(delete("/orders/{id}", orderId)).andExpect(status().isNoContent());

    verify(orderService).deleteOrder(orderId);
  }

  @Test
  void delete_WithInvalidId_ShouldReturnServerError() throws Exception {

    UUID invalidId = UUID.randomUUID();

    doThrow(new RuntimeException("Order not found with id: " + invalidId))
        .when(orderService)
        .deleteOrder(invalidId);

    mockMvc.perform(delete("/orders/{id}", invalidId)).andExpect(status().is5xxServerError());

    verify(orderService).deleteOrder(invalidId);
  }
}
