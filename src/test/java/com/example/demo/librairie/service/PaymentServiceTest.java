package com.example.demo.librairie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.endpoint.event.EventProducer;
import com.example.demo.librairie.dto.PaymentRequest;
import com.example.demo.librairie.dto.PaymentResponse;
import com.example.demo.librairie.entity.Order;
import com.example.demo.librairie.entity.Payment;
import com.example.demo.librairie.entity.PaymentType;
import com.example.demo.librairie.repository.OrderRepository;
import com.example.demo.librairie.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock private PaymentRepository paymentRepository;

  @Mock private OrderRepository orderRepository;
  @Mock private EventProducer eventProducer;
  @InjectMocks private PaymentService paymentService;

  private UUID paymentId;
  private UUID orderId;
  private Order order;
  private Payment payment;
  private PaymentRequest paymentRequest;

  @BeforeEach
  void setUp() {
    paymentId = UUID.randomUUID();
    orderId = UUID.randomUUID();

    order = Order.builder().id(orderId).build();

    payment =
        Payment.builder()
            .id(paymentId)
            .paymentType(PaymentType.CARD)
            .amount(150.0)
            .paymentDate(LocalDateTime.now())
            .order(order)
            .build();

    paymentRequest =
        PaymentRequest.builder()
            .orderId(orderId)
            .paymentType(PaymentType.CARD)
            .amount(150.0)
            .build();
  }

  @Test
  void getAll() {
    when(paymentRepository.findAll()).thenReturn(List.of(payment));

    List<PaymentResponse> result = paymentService.getAll();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(payment.getAmount(), result.get(0).getAmount());
    verify(paymentRepository, times(1)).findAll();
  }

  @Test
  void getById() {
    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

    PaymentResponse result = paymentService.getById(paymentId);

    assertNotNull(result);
    assertEquals(paymentId, result.getId());
    assertEquals(orderId, result.getOrderId());
    verify(paymentRepository, times(1)).findById(paymentId);
  }

  @Test
  void getById_ShouldThrowException_WhenNotFound() {
    when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> paymentService.getById(paymentId));
    verify(paymentRepository, times(1)).findById(paymentId);
  }

  @Test
  void getByOrderId() {
    when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

    PaymentResponse result = paymentService.getByOrderId(orderId);

    assertNotNull(result);
    assertEquals(orderId, result.getOrderId());
    verify(paymentRepository, times(1)).findByOrderId(orderId);
  }

  @Test
  void getByOrderId_ShouldThrowException_WhenNotFound() {
    when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> paymentService.getByOrderId(orderId));
    verify(paymentRepository, times(1)).findByOrderId(orderId);
  }

  @Test
  void create() {
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

    PaymentResponse result = paymentService.create(paymentRequest);

    assertNotNull(result);
    assertEquals(paymentRequest.getAmount(), result.getAmount());
    assertEquals(paymentRequest.getPaymentType(), result.getPaymentType());
    assertEquals(orderId, result.getOrderId());
    verify(orderRepository, times(1)).findById(orderId);
    verify(paymentRepository, times(1)).save(any(Payment.class));
    verify(eventProducer, times(1)).accept(any(List.class)); // ← ajouté
  }

  @Test
  void create_ShouldThrowException_WhenOrderNotFound() {
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> paymentService.create(paymentRequest));
    verify(orderRepository, times(1)).findById(orderId);
    verify(paymentRepository, never()).save(any(Payment.class));
  }

  @Test
  void delete() {
    when(paymentRepository.existsById(paymentId)).thenReturn(true);

    assertDoesNotThrow(() -> paymentService.delete(paymentId));
    verify(paymentRepository, times(1)).existsById(paymentId);
    verify(paymentRepository, times(1)).deleteById(paymentId);
  }

  @Test
  void delete_ShouldThrowException_WhenNotFound() {
    when(paymentRepository.existsById(paymentId)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> paymentService.delete(paymentId));
    verify(paymentRepository, times(1)).existsById(paymentId);
    verify(paymentRepository, never()).deleteById(paymentId);
  }
}
