package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.PaymentRequest;
import com.example.demo.librairie.dto.PaymentResponse;
import com.example.demo.librairie.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  @GetMapping
  public ResponseEntity<List<PaymentResponse>> getAll() {
    return ResponseEntity.ok(paymentService.getAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<PaymentResponse> getById(@PathVariable UUID id) {
    try {
      return ResponseEntity.ok(paymentService.getById(id));
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/order/{orderId}")
  public ResponseEntity<PaymentResponse> getByOrderId(@PathVariable UUID orderId) {
    try {
      return ResponseEntity.ok(paymentService.getByOrderId(orderId));
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping
  public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.create(request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    paymentService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
