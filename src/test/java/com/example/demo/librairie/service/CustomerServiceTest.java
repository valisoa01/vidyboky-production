package com.example.demo.librairie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.librairie.dto.CustomerResponse;
import com.example.demo.librairie.entity.Customer;
import com.example.demo.librairie.exception.ResourceNotFoundException;
import com.example.demo.librairie.repository.CustomerRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

  @Mock private CustomerRepository customerRepository;

  @InjectMocks private CustomerService customerService;

  @Test
  void shouldGetAllCustomers() {

    Customer customer =
        Customer.builder()
            .id(UUID.randomUUID())
            .name("Doe")
            .firstname("John")
            .email("john@mail.com")
            .phone("123456")
            .build();

    when(customerRepository.findAll()).thenReturn(List.of(customer));

    List<CustomerResponse> result = customerService.getAll();

    assertEquals(1, result.size());
  }

  @Test
  void shouldGetCustomerById() {

    UUID id = UUID.randomUUID();

    Customer customer =
        Customer.builder()
            .id(id)
            .name("Doe")
            .firstname("John")
            .email("john@mail.com")
            .phone("123456")
            .build();

    when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

    CustomerResponse result = customerService.getById(id);

    assertEquals(id, result.getId());
  }

  @Test
  void shouldThrowWhenCustomerNotFound() {

    UUID id = UUID.randomUUID();

    when(customerRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> customerService.getById(id));
  }

  @Test
  void shouldDeleteCustomer() {

    UUID id = UUID.randomUUID();

    when(customerRepository.existsById(id)).thenReturn(true);

    customerService.delete(id);

    verify(customerRepository).deleteById(id);
  }

  @Test
  void shouldThrowWhenDeletingUnknownCustomer() {

    UUID id = UUID.randomUUID();

    when(customerRepository.existsById(id)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> customerService.delete(id));
  }
}
