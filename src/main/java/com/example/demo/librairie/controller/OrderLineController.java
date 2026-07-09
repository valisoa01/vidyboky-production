package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.OrderLineRequest;
import com.example.demo.librairie.dto.OrderLineResponse;
import com.example.demo.librairie.service.OrderLineService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order-lines")
@RequiredArgsConstructor
public class OrderLineController {

  private final OrderLineService orderLineService;

  @GetMapping
  public List<OrderLineResponse> getAll() {
    return orderLineService.getAll();
  }

  @GetMapping("/{id}")
  public OrderLineResponse getById(@PathVariable UUID id) {
    return orderLineService.getById(id);
  }

  @PostMapping
  public OrderLineResponse create(@Valid @RequestBody OrderLineRequest request) {
    return orderLineService.create(request);
  }

  @PutMapping("/{id}")
  public OrderLineResponse update(
      @PathVariable UUID id, @Valid @RequestBody OrderLineRequest request) {

    return orderLineService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    orderLineService.delete(id);
  }
}
