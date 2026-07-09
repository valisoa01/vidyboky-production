package com.example.demo.librairie.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.librairie.dto.DeliveryRequest;
import com.example.demo.librairie.dto.DeliveryResponse;
import com.example.demo.librairie.entity.DeliveryStatus;
import com.example.demo.librairie.service.DeliveryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DeliveryController.class)
class DeliveryControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private DeliveryService deliveryService;

    @Autowired private ObjectMapper objectMapper;

    @Test
    void shouldGetAllDeliveries() throws Exception {

        DeliveryResponse delivery =
                DeliveryResponse.builder()
                        .id(UUID.randomUUID())
                        .address("Analakely")
                        .status(DeliveryStatus.PENDING)
                        .expectedDate(LocalDate.now().plusDays(3))
                        .orderId(UUID.randomUUID())
                        .build();

        when(deliveryService.getAll()).thenReturn(List.of(delivery));

        mockMvc
                .perform(get("/deliveries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].address").value("Analakely"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void shouldGetDeliveryById() throws Exception {

        UUID id = UUID.randomUUID();

        DeliveryResponse delivery =
                DeliveryResponse.builder()
                        .id(id)
                        .address("Analakely")
                        .status(DeliveryStatus.PENDING)
                        .expectedDate(LocalDate.now().plusDays(3))
                        .orderId(UUID.randomUUID())
                        .build();

        when(deliveryService.getById(id)).thenReturn(delivery);

        mockMvc
                .perform(get("/deliveries/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.address").value("Analakely"));
    }

    @Test
    void shouldCreateDelivery() throws Exception {

        UUID orderId = UUID.randomUUID();

        DeliveryRequest request =
                DeliveryRequest.builder()
                        .orderId(orderId)
                        .address("Analakely")
                        .status(DeliveryStatus.PENDING)
                        .expectedDate(LocalDate.now().plusDays(3))
                        .build();

        DeliveryResponse response =
                DeliveryResponse.builder()
                        .id(UUID.randomUUID())
                        .orderId(orderId)
                        .address("Analakely")
                        .status(DeliveryStatus.PENDING)
                        .expectedDate(LocalDate.now().plusDays(3))
                        .build();

        when(deliveryService.create(any(DeliveryRequest.class))).thenReturn(response);

        mockMvc
                .perform(
                        post("/deliveries")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.address").value("Analakely"));
    }

    @Test
    void shouldUpdateDelivery() throws Exception {

        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        DeliveryRequest request =
                DeliveryRequest.builder()
                        .orderId(orderId)
                        .address("Ankorondrano")
                        .status(DeliveryStatus.PENDING)
                        .expectedDate(LocalDate.now().plusDays(5))
                        .build();

        DeliveryResponse response =
                DeliveryResponse.builder()
                        .id(id)
                        .orderId(orderId)
                        .address("Ankorondrano")
                        .status(DeliveryStatus.PENDING)
                        .expectedDate(LocalDate.now().plusDays(5))
                        .build();

        when(deliveryService.update(eq(id), any(DeliveryRequest.class))).thenReturn(response);

        mockMvc
                .perform(
                        put("/deliveries/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("Ankorondrano"));
    }

    @Test
    void shouldDeleteDelivery() throws Exception {

        UUID id = UUID.randomUUID();

        doNothing().when(deliveryService).delete(id);

        mockMvc
                .perform(delete("/deliveries/{id}", id))
                .andExpect(status().isNoContent());
    }
}
