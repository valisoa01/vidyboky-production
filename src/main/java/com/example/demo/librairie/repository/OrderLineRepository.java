package com.example.demo.librairie.repository;

import com.example.demo.librairie.entity.OrderLine;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, UUID> {}
