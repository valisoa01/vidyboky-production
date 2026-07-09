package com.example.demo.librairie.service;

import com.example.demo.librairie.dto.DeliveryRequest;
import com.example.demo.librairie.dto.DeliveryResponse;
import com.example.demo.librairie.entity.Delivery;
import com.example.demo.librairie.entity.Order;
import com.example.demo.librairie.entity.OrderType;
import com.example.demo.librairie.exception.DuplicateResourceException;
import com.example.demo.librairie.exception.ResourceNotFoundException;
import com.example.demo.librairie.repository.DeliveryRepository;
import com.example.demo.librairie.repository.OrderRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryService {

  private final DeliveryRepository deliveryRepository;
  private final OrderRepository orderRepository;

  public List<DeliveryResponse> getAll() {
    return deliveryRepository.findAll().stream().map(this::toResponse).toList();
  }

  public DeliveryResponse getById(UUID id) {
    Delivery delivery =
        deliveryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery", id));

    return toResponse(delivery);
  }

  public DeliveryResponse create(@Valid DeliveryRequest request) {

    Order order =
        orderRepository
            .findById(request.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));

    // Une livraison ne peut être créée que pour une commande DELIVERY
    if (order.getOrderType() != OrderType.DELIVERY) {
      throw new IllegalArgumentException("Delivery can only be created for DELIVERY orders.");
    }

    // Une commande ne peut avoir qu'une seule livraison
    if (deliveryRepository.existsByOrderId(request.getOrderId())) {
      throw new DuplicateResourceException("Delivery", "orderId", request.getOrderId().toString());
    }

    Delivery delivery =
        Delivery.builder()
            .address(request.getAddress())
            .status(request.getStatus())
            .expectedDate(request.getExpectedDate())
            .effectiveDate(request.getEffectiveDate())
            .order(order)
            .build();

    return toResponse(deliveryRepository.save(delivery));
  }

  public DeliveryResponse update(UUID id, @Valid DeliveryRequest request) {

    Delivery delivery =
        deliveryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery", id));

    // On ne modifie pas la commande associée à une livraison
    delivery.setAddress(request.getAddress());
    delivery.setStatus(request.getStatus());
    delivery.setExpectedDate(request.getExpectedDate());
    delivery.setEffectiveDate(request.getEffectiveDate());

    return toResponse(deliveryRepository.save(delivery));
  }

  public void delete(UUID id) {

    if (!deliveryRepository.existsById(id)) {
      throw new ResourceNotFoundException("Delivery", id);
    }

    deliveryRepository.deleteById(id);
  }

  private DeliveryResponse toResponse(Delivery delivery) {

    return DeliveryResponse.builder()
        .id(delivery.getId())
        .address(delivery.getAddress())
        .status(delivery.getStatus())
        .expectedDate(delivery.getExpectedDate())
        .effectiveDate(delivery.getEffectiveDate())
        .orderId(delivery.getOrder().getId())
        .build();
  }
}
