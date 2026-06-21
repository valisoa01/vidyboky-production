package com.example.demo.librairie.service;

import com.example.demo.librairie.dto.StockRequest;
import com.example.demo.librairie.dto.StockResponse;
import com.example.demo.librairie.entity.BookFormat;
import com.example.demo.librairie.entity.Stock;
import com.example.demo.librairie.repository.BookFormatRepository;
import com.example.demo.librairie.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final BookFormatRepository bookFormatRepository;

    @Transactional(readOnly = true)
    public List<StockResponse> getAll() {
        return stockRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StockResponse getById(UUID id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock not found with id: " + id));
        return toResponse(stock);
    }


    @Transactional
    public StockResponse create(StockRequest request) {
        BookFormat bookFormat = bookFormatRepository.findById(request.getBookFormatId())
                .orElseThrow(() -> new RuntimeException("BookFormat not found with id: " + request.getBookFormatId()));

        Stock stock = Stock.builder()
                .movement(request.getMovement())
                .quantity(request.getQuantity())
                .movementDate(LocalDateTime.now())
                .bookFormat(bookFormat)
                .build();

        return toResponse(stockRepository.save(stock));
    }

    private StockResponse toResponse(Stock stock) {
        return StockResponse.builder()
                .id(stock.getId())
                .movement(stock.getMovement())
                .quantity(stock.getQuantity())
                .movementDate(stock.getMovementDate())
                .bookFormatId(stock.getBookFormat().getId())
                .build();
    }
}