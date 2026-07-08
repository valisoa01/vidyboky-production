package com.example.demo.librairie.repository;

import com.example.demo.librairie.entity.Genre;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, UUID> {

  @Query(
      "SELECT COALESCE(SUM(ol.quantity * ol.unitPrice), 0) FROM OrderLine ol"
          + " JOIN ol.bookFormat bf JOIN bf.book b JOIN b.genres g WHERE g.id = :genreId")
  Double getRevenueByGenreId(@Param("genreId") UUID genreId);
}
