package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.OrderRequest;
import com.example.demo.librairie.dto.OrderResponse;
import com.example.demo.librairie.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @GetMapping
  public List<OrderResponse> getAll() {
    return orderService.getAllOrders();
  }

  @GetMapping("/{id}")
  public OrderResponse getById(@PathVariable UUID id) {
    return orderService.getOrderById(id);
  }

  @GetMapping("/customer/{customerId}")
  public List<OrderResponse> getByCustomer(@PathVariable UUID customerId) {
    return orderService.getOrdersByCustomer(customerId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrderResponse create(@Valid @RequestBody OrderRequest request) {
    return orderService.createOrder(request);
  }

  @PutMapping("/{id}")
  public OrderResponse update(@PathVariable UUID id, @Valid @RequestBody OrderRequest request) {
    return orderService.updateOrder(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    orderService.deleteOrder(id);
  }
}
