package com.example.demo.librairie.repository;

import com.example.demo.librairie.entity.Format;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormatRepository extends JpaRepository<Format, UUID> {
  Optional<Format> findByFormatType(String formatType);

  boolean existsByFormatType(String formatType);
}
