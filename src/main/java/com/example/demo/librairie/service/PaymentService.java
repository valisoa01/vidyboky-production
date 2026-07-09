package com.example.demo.librairie.service;

import com.example.demo.endpoint.event.EventProducer;
import com.example.demo.endpoint.event.model.InvoiceRequested;
import com.example.demo.librairie.dto.PaymentRequest;
import com.example.demo.librairie.dto.PaymentResponse;
import com.example.demo.librairie.entity.Order;
import com.example.demo.librairie.entity.Payment;
import com.example.demo.librairie.repository.OrderRepository;
import com.example.demo.librairie.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final EventProducer eventProducer;

  public List<PaymentResponse> getAll() {
    return paymentRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
  }

  public PaymentResponse getById(UUID id) {
    Payment payment =
        paymentRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + id));
    return toResponse(payment);
  }

  public PaymentResponse getByOrderId(UUID orderId) {
    Payment payment =
        paymentRepository
            .findByOrderId(orderId)
            .orElseThrow(
                () -> new EntityNotFoundException("Payment not found for order id: " + orderId));
    return toResponse(payment);
  }

  public PaymentResponse create(PaymentRequest request) {
    Order order =
        orderRepository
            .findById(request.getOrderId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Order not found with id: " + request.getOrderId()));

    Payment payment =
        Payment.builder()
            .paymentType(request.getPaymentType())
            .amount(request.getAmount())
            .paymentDate(
                request.getPaymentDate() != null ? request.getPaymentDate() : LocalDateTime.now())
            .order(order)
            .build();

    Payment savedPayment = paymentRepository.save(payment);

    eventProducer.accept(
        List.of(InvoiceRequested.builder().orderId(order.getId().toString()).build()));

    return toResponse(savedPayment);
  }

  public void delete(UUID id) {
    if (!paymentRepository.existsById(id)) {
      throw new EntityNotFoundException("Payment not found with id: " + id);
    }
    paymentRepository.deleteById(id);
  }

  private PaymentResponse toResponse(Payment payment) {
    return PaymentResponse.builder()
        .id(payment.getId())
        .paymentType(payment.getPaymentType())
        .amount(payment.getAmount())
        .paymentDate(payment.getPaymentDate())
        .orderId(payment.getOrder().getId())
        .build();
  }
}
