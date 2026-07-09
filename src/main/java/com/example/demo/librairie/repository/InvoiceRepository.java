package com.example.demo.librairie.repository;

import com.example.demo.librairie.entity.Invoice;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByOrderId(UUID orderId);

    List<Invoice> findByOrder_Customer_IdOrderByGenerationDateDesc(UUID customerId);
}