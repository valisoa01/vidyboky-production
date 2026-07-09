package com.example.demo.librairie.service;

import com.example.demo.librairie.dto.OrderLineRequest;
import com.example.demo.librairie.dto.OrderLineResponse;
import com.example.demo.librairie.entity.BookFormat;
import com.example.demo.librairie.entity.Order;
import com.example.demo.librairie.entity.OrderLine;
import com.example.demo.librairie.repository.BookFormatRepository;
import com.example.demo.librairie.repository.OrderLineRepository;
import com.example.demo.librairie.repository.OrderRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderLineService {

  private final OrderLineRepository orderLineRepository;
  private final OrderRepository orderRepository;
  private final BookFormatRepository bookFormatRepository;

  @Transactional(readOnly = true)
  public List<OrderLineResponse> getAll() {
    return orderLineRepository.findAll().stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public OrderLineResponse getById(UUID id) {
    OrderLine orderLine =
        orderLineRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("OrderLine not found with id: " + id));

    return toResponse(orderLine);
  }

  @Transactional
  public OrderLineResponse create(OrderLineRequest request) {

    Order order =
        orderRepository
            .findById(request.getOrderId())
            .orElseThrow(
                () -> new RuntimeException("Order not found with id: " + request.getOrderId()));

    BookFormat bookFormat =
        bookFormatRepository
            .findById(request.getBookFormatId())
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "BookFormat not found with id: " + request.getBookFormatId()));

    OrderLine orderLine =
        OrderLine.builder()
            .order(order)
            .bookFormat(bookFormat)
            .quantity(request.getQuantity())
            .unitPrice(bookFormat.getPrice())
            .build();

    return toResponse(orderLineRepository.save(orderLine));
  }

  @Transactional
  public OrderLineResponse update(UUID id, OrderLineRequest request) {

    OrderLine orderLine =
        orderLineRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("OrderLine not found with id: " + id));

    BookFormat bookFormat =
        bookFormatRepository
            .findById(request.getBookFormatId())
            .orElseThrow(() -> new RuntimeException("BookFormat not found"));

    orderLine.setBookFormat(bookFormat);
    orderLine.setQuantity(request.getQuantity());
    orderLine.setUnitPrice(bookFormat.getPrice());

    return toResponse(orderLineRepository.save(orderLine));
  }

  @Transactional
  public void delete(UUID id) {

    if (!orderLineRepository.existsById(id)) {
      throw new RuntimeException("OrderLine not found with id: " + id);
    }

    orderLineRepository.deleteById(id);
  }

  private OrderLineResponse toResponse(OrderLine orderLine) {

    return OrderLineResponse.builder()
        .id(orderLine.getId())
        .orderId(orderLine.getOrder().getId())
        .bookFormatId(orderLine.getBookFormat().getId())
        .bookTitle(orderLine.getBookFormat().getBook().getTitle())
        .formatType(orderLine.getBookFormat().getFormat().getFormatType())
        .quantity(orderLine.getQuantity())
        .unitPrice(orderLine.getUnitPrice())
        .totalPrice(orderLine.getQuantity() * orderLine.getUnitPrice())
        .build();
  }
}
