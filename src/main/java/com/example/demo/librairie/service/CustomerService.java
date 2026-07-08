package com.example.demo.librairie.service;

import com.example.demo.librairie.dto.CustomerRequest;
import com.example.demo.librairie.dto.CustomerResponse;
import com.example.demo.librairie.entity.Customer;
import com.example.demo.librairie.exception.DuplicateResourceException;
import com.example.demo.librairie.exception.ResourceNotFoundException;
import com.example.demo.librairie.repository.CustomerRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

  private final CustomerRepository customerRepository;

  public List<CustomerResponse> getAll() {
    return customerRepository.findAll().stream().map(this::toResponse).toList();
  }

  public CustomerResponse getById(UUID id) {
    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

    return toResponse(customer);
  }

  public CustomerResponse create(CustomerRequest request) {

    if (customerRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException("Customer", "email", request.getEmail());
    }

    Customer customer =
        Customer.builder()
            .name(request.getName())
            .firstname(request.getFirstName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .build();

    return toResponse(customerRepository.save(customer));
  }

  public CustomerResponse update(UUID id, CustomerRequest request) {

    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

    customer.setName(request.getName());
    customer.setFirstname(request.getFirstName());
    customer.setEmail(request.getEmail());
    customer.setPhone(request.getPhone());

    return toResponse(customerRepository.save(customer));
  }

  public void delete(UUID id) {

    if (!customerRepository.existsById(id)) {
      throw new ResourceNotFoundException("Customer", id);
    }

    customerRepository.deleteById(id);
  }

  private CustomerResponse toResponse(Customer customer) {
    return CustomerResponse.builder()
        .id(customer.getId())
        .name(customer.getName())
        .firstName(customer.getFirstname())
        .email(customer.getEmail())
        .phone(customer.getPhone())
        .build();
  }
}
