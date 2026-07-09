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

import com.example.demo.librairie.dto.CustomerRequest;
import com.example.demo.librairie.dto.CustomerResponse;
import com.example.demo.librairie.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private CustomerService customerService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldGetAllCustomers() throws Exception {
    CustomerResponse customer =
        CustomerResponse.builder()
            .id(UUID.randomUUID())
            .firstName("John")
            .name("Doe")
            .email("john@mail.com")
            .phone("123456")
            .build();

    when(customerService.getAll()).thenReturn(List.of(customer));

    mockMvc
        .perform(get("/customers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Doe"));
  }

  @Test
  void shouldGetCustomerById() throws Exception {
    UUID id = UUID.randomUUID();

    CustomerResponse customer =
        CustomerResponse.builder()
            .id(id)
            .firstName("John")
            .name("Doe")
            .email("john@mail.com")
            .phone("123456")
            .build();

    when(customerService.getById(id)).thenReturn(customer);

    mockMvc
        .perform(get("/customers/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.name").value("Doe"));
  }

  @Test
  void shouldCreateCustomer() throws Exception {
    CustomerRequest request =
        CustomerRequest.builder()
            .firstName("Ndrina")
            .name("Rakotobe")
            .email("ndrina@mail.com")
            .phone("0321456987")
            .build();

    CustomerResponse response =
        CustomerResponse.builder()
            .id(UUID.randomUUID())
            .firstName("Ndrina")
            .name("Rakotobe")
            .email("ndrina@mail.com")
            .phone("0321456987")
            .build();

    when(customerService.create(any(CustomerRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Rakotobe"));
  }

  @Test
  void shouldUpdateCustomer() throws Exception {
    UUID id = UUID.randomUUID();

    CustomerRequest request =
        CustomerRequest.builder()
            .firstName("Ndrina")
            .name("Rakoto")
            .email("ndrina@mail.com")
            .phone("0321456987")
            .build();

    CustomerResponse response =
        CustomerResponse.builder()
            .id(id)
            .firstName("Ndrina")
            .name("Rakoto")
            .email("ndrina@mail.com")
            .phone("0321456987")
            .build();

    when(customerService.update(eq(id), any(CustomerRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            put("/customers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Rakoto"));
  }

  @Test
  void shouldDeleteCustomer() throws Exception {
    UUID id = UUID.randomUUID();

    doNothing().when(customerService).delete(id);

    mockMvc.perform(delete("/customers/{id}", id)).andExpect(status().isOk());
  }
}
