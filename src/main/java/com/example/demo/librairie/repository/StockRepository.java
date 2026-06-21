package com.example.demo.librairie.repository;

import com.example.demo.librairie.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {
    List<Stock> findByBookFormatId(UUID bookFormatId);
}