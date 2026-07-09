package com.example.demo.librairie.repository;

import com.example.demo.librairie.entity.Delivery;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    boolean existsByOrderId(UUID orderId);

}