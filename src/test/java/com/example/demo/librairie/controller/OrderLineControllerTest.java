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
import com.example.demo.librairie.service.OrderLineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderLineController.class)
class OrderLineControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private OrderLineService orderLineService;

  private UUID orderLineId;
  private UUID orderId;
  private UUID bookFormatId;
  private OrderLineRequest orderLineRequest;
  private OrderLineResponse orderLineResponse;
  private List<OrderLineResponse> orderLineResponseList;

  @BeforeEach
  void setUp() {
    orderLineId = UUID.randomUUID();
    orderId = UUID.randomUUID();
    bookFormatId = UUID.randomUUID();

    orderLineRequest =
        OrderLineRequest.builder().orderId(orderId).bookFormatId(bookFormatId).quantity(3).build();

    orderLineResponse =
        OrderLineResponse.builder()
            .id(orderLineId)
            .orderId(orderId)
            .bookFormatId(bookFormatId)
            .bookTitle("Les Misérables")
            .formatType("Poche")
            .quantity(3)
            .unitPrice(12.99)
            .totalPrice(38.97)
            .build();

    OrderLineResponse orderLineResponse2 =
        OrderLineResponse.builder()
            .id(UUID.randomUUID())
            .orderId(orderId)
            .bookFormatId(UUID.randomUUID())
            .bookTitle("L'Étranger")
            .formatType("Relié")
            .quantity(1)
            .unitPrice(24.99)
            .totalPrice(24.99)
            .build();

    orderLineResponseList = List.of(orderLineResponse, orderLineResponse2);
  }

  @Test
  void getAll_ShouldReturnListOfOrderLines() throws Exception {
    when(orderLineService.getAll()).thenReturn(orderLineResponseList);

    mockMvc
        .perform(get("/order-lines").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id").value(orderLineId.toString()))
        .andExpect(jsonPath("$[0].bookTitle").value("Les Misérables"))
        .andExpect(jsonPath("$[0].formatType").value("Poche"))
        .andExpect(jsonPath("$[0].quantity").value(3))
        .andExpect(jsonPath("$[0].unitPrice").value(12.99))
        .andExpect(jsonPath("$[0].totalPrice").value(38.97))
        .andExpect(jsonPath("$[1].bookTitle").value("L'Étranger"))
        .andExpect(jsonPath("$[1].formatType").value("Relié"))
        .andExpect(jsonPath("$[1].quantity").value(1));

    verify(orderLineService).getAll();
  }

  @Test
  void getById_WithValidId_ShouldReturnOrderLine() throws Exception {
    when(orderLineService.getById(orderLineId)).thenReturn(orderLineResponse);

    mockMvc
        .perform(get("/order-lines/{id}", orderLineId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderLineId.toString()))
        .andExpect(jsonPath("$.orderId").value(orderId.toString()))
        .andExpect(jsonPath("$.bookFormatId").value(bookFormatId.toString()))
        .andExpect(jsonPath("$.bookTitle").value("Les Misérables"))
        .andExpect(jsonPath("$.formatType").value("Poche"))
        .andExpect(jsonPath("$.quantity").value(3))
        .andExpect(jsonPath("$.unitPrice").value(12.99))
        .andExpect(jsonPath("$.totalPrice").value(38.97));

    verify(orderLineService).getById(orderLineId);
  }

  @Test
  void getById_WithInvalidId_ShouldReturnServerError() throws Exception {
    UUID invalidId = UUID.randomUUID();
    when(orderLineService.getById(invalidId))
        .thenThrow(new RuntimeException("OrderLine not found with id: " + invalidId));

    mockMvc
        .perform(get("/order-lines/{id}", invalidId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is5xxServerError());

    verify(orderLineService).getById(invalidId);
  }

  @Test
  void create_WithValidRequest_ShouldReturnCreatedOrderLine() throws Exception {
    when(orderLineService.create(any(OrderLineRequest.class))).thenReturn(orderLineResponse);

    mockMvc
        .perform(
            post("/order-lines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderLineRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderLineId.toString()))
        .andExpect(jsonPath("$.orderId").value(orderId.toString()))
        .andExpect(jsonPath("$.bookFormatId").value(bookFormatId.toString()))
        .andExpect(jsonPath("$.quantity").value(3))
        .andExpect(jsonPath("$.totalPrice").value(38.97));

    verify(orderLineService).create(any(OrderLineRequest.class));
  }

  @Test
  void create_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
    OrderLineRequest invalidRequest =
        OrderLineRequest.builder()
            .orderId(orderId)
            .bookFormatId(bookFormatId)
            .quantity(null)
            .build();

    mockMvc
        .perform(
            post("/order-lines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void create_WithNonPositiveQuantity_ShouldReturnBadRequest() throws Exception {
    OrderLineRequest invalidRequest =
        OrderLineRequest.builder().orderId(orderId).bookFormatId(bookFormatId).quantity(0).build();

    mockMvc
        .perform(
            post("/order-lines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void update_WithValidIdAndRequest_ShouldReturnUpdatedOrderLine() throws Exception {
    OrderLineResponse updatedResponse =
        OrderLineResponse.builder()
            .id(orderLineId)
            .orderId(orderId)
            .bookFormatId(bookFormatId)
            .bookTitle("Les Misérables")
            .formatType("Poche")
            .quantity(5)
            .unitPrice(12.99)
            .totalPrice(64.95)
            .build();

    when(orderLineService.update(eq(orderLineId), any(OrderLineRequest.class)))
        .thenReturn(updatedResponse);

    mockMvc
        .perform(
            put("/order-lines/{id}", orderLineId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderLineRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderLineId.toString()))
        .andExpect(jsonPath("$.quantity").value(5))
        .andExpect(jsonPath("$.totalPrice").value(64.95));

    verify(orderLineService).update(eq(orderLineId), any(OrderLineRequest.class));
  }

  @Test
  void update_WithInvalidId_ShouldReturnServerError() throws Exception {
    UUID invalidId = UUID.randomUUID();
    when(orderLineService.update(eq(invalidId), any(OrderLineRequest.class)))
        .thenThrow(new RuntimeException("OrderLine not found with id: " + invalidId));

    mockMvc
        .perform(
            put("/order-lines/{id}", invalidId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderLineRequest)))
        .andExpect(status().is5xxServerError());

    verify(orderLineService).update(eq(invalidId), any(OrderLineRequest.class));
  }

  @Test
  void update_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
    OrderLineRequest invalidRequest =
        OrderLineRequest.builder().orderId(orderId).bookFormatId(bookFormatId).quantity(-1).build();

    mockMvc
        .perform(
            put("/order-lines/{id}", orderLineId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void delete_WithValidId_ShouldReturnOk() throws Exception {
    doNothing().when(orderLineService).delete(orderLineId);

    mockMvc
        .perform(delete("/order-lines/{id}", orderLineId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(orderLineService).delete(orderLineId);
  }

  @Test
  void delete_WithInvalidId_ShouldReturnServerError() throws Exception {
    UUID invalidId = UUID.randomUUID();
    doThrow(new RuntimeException("OrderLine not found with id: " + invalidId))
        .when(orderLineService)
        .delete(invalidId);

    mockMvc
        .perform(delete("/order-lines/{id}", invalidId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is5xxServerError());

    verify(orderLineService).delete(invalidId);
  }
}
