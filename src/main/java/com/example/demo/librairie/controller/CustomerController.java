package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.CustomerRequest;
import com.example.demo.librairie.dto.CustomerResponse;
import com.example.demo.librairie.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<CustomerResponse> createCustomer( @RequestBody CustomerRequest request) {
        CustomerResponse created = customerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable UUID id,
            @RequestBody CustomerRequest request) {
        CustomerResponse updated = customerService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteCustomer(@PathVariable UUID id) {
        customerService.delete(id);
    }
}
