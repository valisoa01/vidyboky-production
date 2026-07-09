package com.example.demo.librairie.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.demo.librairie.dto.PaymentRequest;
import com.example.demo.librairie.dto.PaymentResponse;
import com.example.demo.librairie.entity.PaymentType;
import com.example.demo.librairie.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PaymentService paymentService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void getAll() throws Exception {
    List<PaymentResponse> payments = List.of(new PaymentResponse());
    when(paymentService.getAll()).thenReturn(payments);

    mockMvc.perform(get("/payments")).andExpect(status().isOk());
  }

  @Test
  void getById() throws Exception {
    UUID id = UUID.randomUUID();
    PaymentResponse response = new PaymentResponse();
    when(paymentService.getById(id)).thenReturn(response);

    mockMvc.perform(get("/payments/{id}", id)).andExpect(status().isOk());
  }

  @Test
  void getById_NotFound() throws Exception {
    UUID id = UUID.randomUUID();
    when(paymentService.getById(id)).thenThrow(new RuntimeException("Not found"));

    mockMvc.perform(get("/payments/{id}", id)).andExpect(status().isNotFound());
  }

  @Test
  void getByOrderId() throws Exception {
    UUID orderId = UUID.randomUUID();
    PaymentResponse response = new PaymentResponse();
    when(paymentService.getByOrderId(orderId)).thenReturn(response);

    mockMvc.perform(get("/payments/order/{orderId}", orderId)).andExpect(status().isOk());
  }

  @Test
  void create() throws Exception {
    PaymentRequest request = new PaymentRequest();
    request.setOrderId(UUID.randomUUID());
    request.setPaymentType(PaymentType.CARD);
    request.setAmount(25000.0);
    request.setPaymentDate(LocalDateTime.now());

    PaymentResponse response = new PaymentResponse();
    response.setId(UUID.randomUUID());

    when(paymentService.create(any(PaymentRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists());
  }

  @Test
  void delete() throws Exception {
    UUID id = UUID.randomUUID();
    doNothing().when(paymentService).delete(id);

    mockMvc
        .perform(MockMvcRequestBuilders.delete("/payments/{id}", id))
        .andExpect(status().isNoContent());

    verify(paymentService).delete(id);
  }
}
