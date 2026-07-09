package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.CustomerRequest;
import com.example.demo.librairie.dto.CustomerResponse;
import com.example.demo.librairie.service.CustomerService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {
  private final CustomerService customerService;

  @GetMapping
  public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
    List<CustomerResponse> customers = customerService.getAll();
    return ResponseEntity.ok(customers);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable UUID id) {
    CustomerResponse customer = customerService.getById(id);
    return ResponseEntity.ok(customer);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
    return customerService.create(request);
  }

  @PutMapping("/{id}")
  public CustomerResponse update(
      @PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
    return customerService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public void deleteCustomer(@PathVariable UUID id) {
    customerService.delete(id);
  }
}
