package com.example.demo.librairie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.librairie.dto.DeliveryRequest;
import com.example.demo.librairie.dto.DeliveryResponse;
import com.example.demo.librairie.entity.Delivery;
import com.example.demo.librairie.entity.DeliveryStatus;
import com.example.demo.librairie.entity.Order;
import com.example.demo.librairie.entity.OrderType;
import com.example.demo.librairie.exception.DuplicateResourceException;
import com.example.demo.librairie.exception.ResourceNotFoundException;
import com.example.demo.librairie.repository.DeliveryRepository;
import com.example.demo.librairie.repository.OrderRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock private DeliveryRepository deliveryRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks private DeliveryService deliveryService;

    @Test
    void shouldGetAllDeliveries() {

        Order order = Order.builder().id(UUID.randomUUID()).build();

        Delivery delivery1 =
                Delivery.builder()
                        .id(UUID.randomUUID())
                        .address("Avenue de l'Indépendance, Analakely")
                        .status(DeliveryStatus.PENDING)
                        .expectedDate(LocalDate.now().plusDays(2))
                        .order(order)
                        .build();

        Delivery delivery2 =
                Delivery.builder()
                        .id(UUID.randomUUID())
                        .address("Ankorondrano")
                        .status(DeliveryStatus.SHIPPED)
                        .expectedDate(LocalDate.now().plusDays(1))
                        .order(order)
                        .build();

        when(deliveryRepository.findAll()).thenReturn(List.of(delivery1, delivery2));

        List<DeliveryResponse> result = deliveryService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Avenue de l'Indépendance, Analakely", result.get(0).getAddress());
        assertEquals("Ankorondrano", result.get(1).getAddress());
    }

    @Test
    void shouldGetDeliveryById() {

        UUID id = UUID.randomUUID();
        Order order = Order.builder().id(UUID.randomUUID()).build();

        Delivery delivery =
                Delivery.builder()
                        .id(id)
                        .address("Ivandry")
                        .status(DeliveryStatus.PENDING)
                        .expectedDate(LocalDate.now().plusDays(3))
                        .order(order)
                        .build();

        when(deliveryRepository.findById(id)).thenReturn(Optional.of(delivery));

        DeliveryResponse result = deliveryService.getById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Ivandry", result.getAddress());
    }

    @Test
    void shouldThrowExceptionWhenDeliveryNotFound() {

        UUID id = UUID.randomUUID();
        when(deliveryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> deliveryService.getById(id));
    }

    @Test
    void shouldCreateDelivery() {

        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).orderType(OrderType.DELIVERY).build();

        DeliveryRequest request =
                DeliveryRequest.builder()
                        .orderId(orderId)
                        .address("Itaosy")
                        .status(DeliveryStatus.PENDING)
                        .expectedDate(LocalDate.now().plusDays(4))
                        .build();

        Delivery savedDelivery =
                Delivery.builder()
                        .id(UUID.randomUUID())
                        .address("Itaosy")
                        .status(DeliveryStatus.PENDING)
                        .expectedDate(LocalDate.now().plusDays(4))
                        .order(order)
                        .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(deliveryRepository.existsByOrderId(orderId)).thenReturn(false);
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(savedDelivery);

        DeliveryResponse result = deliveryService.create(request);

        assertNotNull(result);
        assertEquals("Itaosy", result.getAddress());
        verify(deliveryRepository, times(1)).save(any(Delivery.class));
    }

    @Test
    void shouldThrowExceptionWhenOrderTypeIsDownload() {

        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).orderType(OrderType.DOWNLOAD).build();

        DeliveryRequest request =
                DeliveryRequest.builder()
                        .orderId(orderId)
                        .address("Itaosy")
                        .status(DeliveryStatus.PENDING)
                        .expectedDate(LocalDate.now().plusDays(4))
                        .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> deliveryService.create(request));
        verify(deliveryRepository, never()).save(any(Delivery.class));
    }

    @Test
    void shouldThrowExceptionWhenDeliveryAlreadyExistsForOrder() {

        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).orderType(OrderType.DELIVERY).build();

        DeliveryRequest request =
                DeliveryRequest.builder()
                        .orderId(orderId)
                        .address("Itaosy")
                        .status(DeliveryStatus.PENDING)
                        .expectedDate(LocalDate.now().plusDays(4))
                        .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(deliveryRepository.existsByOrderId(orderId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> deliveryService.create(request));
        verify(deliveryRepository, never()).save(any(Delivery.class));
    }

    @Test
    void shouldUpdateDelivery() {

        UUID id = UUID.randomUUID();
        Order order = Order.builder().id(UUID.randomUUID()).build();

        Delivery existingDelivery =
                Delivery.builder()
                        .id(id)
                        .address("Behoririka")
                        .status(DeliveryStatus.PENDING)
                        .expectedDate(LocalDate.now().plusDays(1))
                        .order(order)
                        .build();

        DeliveryRequest request =
                DeliveryRequest.builder()
                        .address("Ankorondrano")
                        .status(DeliveryStatus.SHIPPED)
                        .expectedDate(LocalDate.now().plusDays(3))
                        .build();

        Delivery updatedDelivery =
                Delivery.builder()
                        .id(id)
                        .address("Ankorondrano")
                        .status(DeliveryStatus.SHIPPED)
                        .expectedDate(LocalDate.now().plusDays(3))
                        .order(order)
                        .build();

        when(deliveryRepository.findById(id)).thenReturn(Optional.of(existingDelivery));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(updatedDelivery);

        DeliveryResponse result = deliveryService.update(id, request);

        assertNotNull(result);
        assertEquals("Ankorondrano", result.getAddress());
        assertEquals(DeliveryStatus.SHIPPED, result.getStatus());
    }

    @Test
    void shouldDeleteDelivery() {

        UUID id = UUID.randomUUID();
        when(deliveryRepository.existsById(id)).thenReturn(true);
        doNothing().when(deliveryRepository).deleteById(id);

        assertDoesNotThrow(() -> deliveryService.delete(id));
        verify(deliveryRepository, times(1)).deleteById(id);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentDelivery() {

        UUID id = UUID.randomUUID();
        when(deliveryRepository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> deliveryService.delete(id));
        verify(deliveryRepository, never()).deleteById(any());
    }
}