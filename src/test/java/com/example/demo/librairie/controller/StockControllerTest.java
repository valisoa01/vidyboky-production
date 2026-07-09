package com.example.demo.librairie.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.demo.librairie.dto.StockRequest;
import com.example.demo.librairie.dto.StockResponse;
import com.example.demo.librairie.entity.MovementType;
import com.example.demo.librairie.service.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StockController.class)
class StockControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private StockService stockService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void getAll() throws Exception {
    List<StockResponse> stocks = List.of(new StockResponse());
    when(stockService.getAll()).thenReturn(stocks);

    mockMvc.perform(get("/stocks")).andExpect(status().isOk());
  }

  @Test
  void getById() throws Exception {
    UUID id = UUID.randomUUID();
    StockResponse response = new StockResponse();
    when(stockService.getById(id)).thenReturn(response);

    mockMvc.perform(get("/stocks/{id}", id)).andExpect(status().isOk());
  }

  @Test
  void getByBookFormatId() throws Exception {
    UUID bookFormatId = UUID.randomUUID();
    List<StockResponse> stocks = List.of(new StockResponse());
    when(stockService.getByBookFormatId(bookFormatId)).thenReturn(stocks);

    mockMvc
        .perform(get("/stocks/book-format/{bookFormatId}", bookFormatId))
        .andExpect(status().isOk());
  }

  @Test
  void getCurrentStock() throws Exception {
    UUID bookFormatId = UUID.randomUUID();
    when(stockService.getCurrentStock(bookFormatId)).thenReturn(42);

    mockMvc
        .perform(get("/stocks/book-format/{bookFormatId}/current-stock", bookFormatId))
        .andExpect(status().isOk())
        .andExpect(content().string("42"));
  }

  @Test
  void create() throws Exception {
    // Create a valid request with all required fields
    StockRequest request =
        StockRequest.builder()
            .bookFormatId(UUID.randomUUID())
            .movement(MovementType.IN)
            .quantity(10)
            .build();

    StockResponse response =
        StockResponse.builder()
            .id(UUID.randomUUID())
            .bookFormatId(request.getBookFormatId())
            .movement(request.getMovement())
            .quantity(request.getQuantity())
            .movementDate(LocalDateTime.now())
            .build();

    when(stockService.create(any(StockRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/stocks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.bookFormatId").value(request.getBookFormatId().toString()))
        .andExpect(jsonPath("$.movement").value(request.getMovement().toString()))
        .andExpect(jsonPath("$.quantity").value(request.getQuantity()));
  }

  @Test
  void getStockSummary() throws Exception {
    Map<String, Integer> summary = Map.of("Paperback", 50, "Hardcover", 30);
    when(stockService.getStockSummary()).thenReturn(summary);

    mockMvc.perform(get("/stocks/current-stock/summary")).andExpect(status().isOk());
  }
}
