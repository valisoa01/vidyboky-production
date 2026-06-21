package com.example.demo.librairie.repository;

import com.example.demo.librairie.entity.Stock;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {
  List<Stock> findByBookFormatId(UUID bookFormatId);
}
