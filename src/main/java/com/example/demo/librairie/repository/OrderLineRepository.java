package com.example.demo.librairie.repository;

import com.example.demo.librairie.entity.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, UUID> {

}
