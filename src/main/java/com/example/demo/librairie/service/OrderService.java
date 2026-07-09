package com.example.demo.librairie.service;

import com.example.demo.librairie.dto.OrderLineRequest;
import com.example.demo.librairie.dto.OrderLineResponse;
import com.example.demo.librairie.dto.OrderRequest;
import com.example.demo.librairie.dto.OrderResponse;
import com.example.demo.librairie.entity.*;
import com.example.demo.librairie.exception.ResourceNotFoundException;
import com.example.demo.librairie.repository.BookFormatRepository;
import com.example.demo.librairie.repository.CustomerRepository;
import com.example.demo.librairie.repository.OrderRepository;
import com.example.demo.librairie.repository.StockRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final CustomerRepository customerRepository;
  private final BookFormatRepository bookFormatRepository;
  private final StockService stockService;
  private final StockRepository stockRepository;

  @Transactional(readOnly = true)
  public List<OrderResponse> getAllOrders() {
    return orderRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public OrderResponse getOrderById(UUID id) {
    Order order =
        orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order", id));
    return toResponse(order);
  }

  @Transactional(readOnly = true)
  public List<OrderResponse> getOrdersByCustomer(UUID customerId) {
    if (!customerRepository.existsById(customerId)) {
      throw new ResourceNotFoundException("Customer", customerId);
    }
    return orderRepository.findByCustomerId(customerId).stream().map(this::toResponse).toList();
  }

  @Transactional
  public OrderResponse createOrder(OrderRequest request) {
    Customer customer =
        customerRepository
            .findById(request.getCustomerId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));

    List<OrderLine> orderLines = new ArrayList<>();

    for (OrderLineRequest lineRequest : request.getLines()) {
      BookFormat bookFormat =
          bookFormatRepository
              .findById(lineRequest.getBookFormatId())
              .orElseThrow(
                  () -> new ResourceNotFoundException("BookFormat", lineRequest.getBookFormatId()));

      Integer currentStock = stockService.getCurrentStock(bookFormat.getId());

      if (lineRequest.getQuantity() > currentStock) {
        throw new IllegalArgumentException(
            "Insufficient stock for book: "
                + bookFormat.getBook().getTitle()
                + " (Requested: "
                + lineRequest.getQuantity()
                + ", Available: "
                + currentStock
                + ")");
      }

      StockMovement stockMovement =
          StockMovement.builder()
              .movement(MovementType.OUT)
              .quantity(lineRequest.getQuantity())
              .movementDate(LocalDateTime.now())
              .bookFormat(bookFormat)
              .build();
      stockRepository.save(stockMovement);

      OrderLine orderLine =
          OrderLine.builder()
              .bookFormat(bookFormat)
              .quantity(lineRequest.getQuantity())
              .unitPrice(bookFormat.getPrice())
              .build();

      orderLines.add(orderLine);
    }

    Order order =
        Order.builder()
            .customer(customer)
            .orderType(request.getOrderType())
            .orderDate(
                request.getOrderDate() != null ? request.getOrderDate() : LocalDateTime.now())
            .lines(orderLines)
            .build();

    orderLines.forEach(line -> line.setOrder(order));
    Order savedOrder = orderRepository.save(order);

    double totalAmount =
        savedOrder.getLines().stream()
            .mapToDouble(line -> line.getUnitPrice() * line.getQuantity())
            .sum();

    List<OrderLineResponse> lineResponses =
        savedOrder.getLines().stream()
            .map(
                line ->
                    OrderLineResponse.builder()
                        .id(line.getId())
                        .bookFormatId(line.getBookFormat().getId())
                        .bookTitle(line.getBookFormat().getBook().getTitle())
                        .formatType(line.getBookFormat().getFormat().getFormatType())
                        .quantity(line.getQuantity())
                        .unitPrice(line.getUnitPrice())
                        .lineTotal(line.getUnitPrice() * line.getQuantity())
                        .build())
            .collect(Collectors.toList());

    return OrderResponse.builder()
        .id(savedOrder.getId())
        .orderType(savedOrder.getOrderType())
        .orderDate(savedOrder.getOrderDate())
        .customerId(savedOrder.getCustomer().getId())
        .customerFullName(
            savedOrder.getCustomer().getFirstname() + " " + savedOrder.getCustomer().getName())
        .lines(lineResponses)
        .totalAmount(totalAmount)
        .build();
  }

  @Transactional
  public OrderResponse updateOrder(UUID id, OrderRequest request) {
    Order existingOrder =
        orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order", id));

    Customer customer =
        customerRepository
            .findById(request.getCustomerId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));

    existingOrder.setOrderType(request.getOrderType());
    existingOrder.setOrderDate(
        request.getOrderDate() != null ? request.getOrderDate() : existingOrder.getOrderDate());
    existingOrder.setCustomer(customer);
    existingOrder.getLines().clear();
    existingOrder.getLines().addAll(buildLines(request.getLines(), existingOrder));

    return toResponse(orderRepository.save(existingOrder));
  }

  @Transactional
  public void deleteOrder(UUID id) {
    if (!orderRepository.existsById(id)) {
      throw new ResourceNotFoundException("Order", id);
    }
    orderRepository.deleteById(id);
  }

  private List<OrderLine> buildLines(List<OrderLineRequest> lineRequests, Order order) {
    List<OrderLine> lines = new ArrayList<>();
    for (OrderLineRequest lineRequest : lineRequests) {
      BookFormat bookFormat =
          bookFormatRepository
              .findById(lineRequest.getBookFormatId())
              .orElseThrow(
                  () -> new ResourceNotFoundException("BookFormat", lineRequest.getBookFormatId()));

      lines.add(
          OrderLine.builder()
              .order(order)
              .bookFormat(bookFormat)
              .quantity(lineRequest.getQuantity())
              .unitPrice(bookFormat.getPrice())
              .build());
    }
    return lines;
  }

  private OrderResponse toResponse(Order order) {
    List<OrderLineResponse> lineResponses =
        order.getLines() == null
            ? List.of()
            : order.getLines().stream().map(this::toLineResponse).toList();

    double totalAmount = lineResponses.stream().mapToDouble(OrderLineResponse::getLineTotal).sum();

    return OrderResponse.builder()
        .id(order.getId())
        .orderType(order.getOrderType())
        .orderDate(order.getOrderDate())
        .customerId(order.getCustomer().getId())
        .customerFullName(order.getCustomer().getFirstname() + " " + order.getCustomer().getName())
        .lines(lineResponses)
        .totalAmount(totalAmount)
        .build();
  }

  private OrderLineResponse toLineResponse(OrderLine line) {
    return OrderLineResponse.builder()
        .id(line.getId())
        .bookFormatId(line.getBookFormat().getId())
        .bookTitle(line.getBookFormat().getBook().getTitle())
        .formatType(line.getBookFormat().getFormat().getFormatType())
        .quantity(line.getQuantity())
        .unitPrice(line.getUnitPrice())
        .lineTotal(line.getUnitPrice() * line.getQuantity())
        .build();
  }
}
