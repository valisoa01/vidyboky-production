package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.StockResponse;
import com.example.demo.librairie.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

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
}