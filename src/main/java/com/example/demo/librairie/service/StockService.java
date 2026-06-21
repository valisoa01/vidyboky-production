package com.example.demo.librairie.service;

import com.example.demo.librairie.dto.StockRequest;
import com.example.demo.librairie.dto.StockResponse;
import com.example.demo.librairie.entity.BookFormat;
import com.example.demo.librairie.entity.MovementType;
import com.example.demo.librairie.entity.Stock;
import com.example.demo.librairie.repository.BookFormatRepository;
import com.example.demo.librairie.repository.StockRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

  private final StockRepository stockRepository;
  private final BookFormatRepository bookFormatRepository;

  @Transactional(readOnly = true)
  public List<StockResponse> getAll() {
    return stockRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public StockResponse getById(UUID id) {
    Stock stock =
        stockRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Stock not found with id: " + id));
    return toResponse(stock);
  }

  @Transactional(readOnly = true)
  public List<StockResponse> getByBookFormatId(UUID bookFormatId) {
    if (!bookFormatRepository.existsById(bookFormatId)) {
      throw new RuntimeException("BookFormat not found with id: " + bookFormatId);
    }

    return stockRepository.findByBookFormatId(bookFormatId).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public Integer getCurrentStock(UUID bookFormatId) {
    if (!bookFormatRepository.existsById(bookFormatId)) {
      throw new RuntimeException("BookFormat not found with id: " + bookFormatId);
    }

    List<Stock> stocks = stockRepository.findByBookFormatId(bookFormatId);

    return stocks.stream()
        .mapToInt(
            stock -> {
              if (stock.getMovement() == MovementType.IN) {
                return stock.getQuantity();
              } else { // OUT
                return -stock.getQuantity();
              }
            })
        .sum();
  }

  @Transactional
  public StockResponse create(StockRequest request) {
    BookFormat bookFormat =
        bookFormatRepository
            .findById(request.getBookFormatId())
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "BookFormat not found with id: " + request.getBookFormatId()));

    if (request.getMovement() == MovementType.OUT) {
      Integer currentStock = getCurrentStock(request.getBookFormatId());
      if (currentStock < request.getQuantity()) {
        throw new RuntimeException(
            "Stock insuffisant ! Stock actuel : "
                + currentStock
                + ", demandé : "
                + request.getQuantity());
      }
    }

    Stock stock =
        Stock.builder()
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
        .bookTitle(stock.getBookFormat().getBook().getTitle())
        .formatType(stock.getBookFormat().getFormat().getFormatType())
        .build();
  }

  @Transactional(readOnly = true)
  public Map<String, Integer> getStockSummary() {
    List<BookFormat> bookFormats = bookFormatRepository.findAll();
    Map<String, Integer> summary = new HashMap<>();

    for (BookFormat bookFormat : bookFormats) {
      String formatType = bookFormat.getFormat().getFormatType();
      Integer currentStock = getCurrentStock(bookFormat.getId());

      if (summary.containsKey(formatType)) {
        summary.put(formatType, summary.get(formatType) + currentStock);
      } else {
        summary.put(formatType, currentStock);
      }
    }

    return summary;
  }
}
