package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.StockResponse;
import com.example.demo.librairie.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}