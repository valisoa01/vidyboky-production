package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.StockRequest;
import com.example.demo.librairie.dto.StockResponse;
import com.example.demo.librairie.service.StockService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockController {

  private final StockService stockService;

  @GetMapping
  public ResponseEntity<List<StockResponse>> getAll() {
    List<StockResponse> stocks = stockService.getAll();
    return ResponseEntity.ok(stocks);
  }

  @GetMapping("/{id}")
  public ResponseEntity<StockResponse> getById(@PathVariable UUID id) {
    try {
      StockResponse stock = stockService.getById(id);
      return ResponseEntity.ok(stock);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/book-format/{bookFormatId}")
  public ResponseEntity<List<StockResponse>> getByBookFormatId(@PathVariable UUID bookFormatId) {
    try {
      List<StockResponse> stocks = stockService.getByBookFormatId(bookFormatId);
      return ResponseEntity.ok(stocks);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/book-format/{bookFormatId}/current-stock")
  public ResponseEntity<Integer> getCurrentStock(@PathVariable UUID bookFormatId) {
    try {
      Integer currentStock = stockService.getCurrentStock(bookFormatId);
      return ResponseEntity.ok(currentStock);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping
  public ResponseEntity<StockResponse> create(@Valid @RequestBody StockRequest request) {
    try {
      StockResponse response = stockService.create(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (RuntimeException e) {
      String message = e.getMessage();

      if (message != null && message.startsWith("BookFormat not found")) {
        return ResponseEntity.notFound().build();
      }

      if (message != null && message.startsWith("Stock insuffisant")) {
        return ResponseEntity.badRequest().build();
      }

      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/current-stock/summary")
  public ResponseEntity<Map<String, Integer>> getStockSummary() {
    Map<String, Integer> summary = stockService.getStockSummary();
    return ResponseEntity.ok(summary);
  }
}
