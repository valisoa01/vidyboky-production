package com.example.demo.librairie.repository;

import com.example.demo.librairie.entity.BookFormat;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface BookFormatRepository extends JpaRepository<BookFormat, UUID> {

  List<BookFormat> findByBookId(UUID bookId);

  List<BookFormat> findByFormatId(UUID formatId);

  Optional<BookFormat> findByBookIdAndFormatId(UUID bookId, UUID formatId);

  List<BookFormat> findByPriceLessThanEqual(BigDecimal maxPrice);

  List<BookFormat> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

  boolean existsByBookIdAndFormatId(UUID bookId, UUID formatId);

  @Query("SELECT bf FROM BookFormat bf WHERE LOWER(bf.format.formatType) = LOWER(:formatType)")
  List<BookFormat> findByFormatType(@Param("formatType") String formatType);

  @Query(
      "SELECT bf FROM BookFormat bf WHERE LOWER(bf.book.title) LIKE LOWER(CONCAT('%', :title,"
          + " '%'))")
  List<BookFormat> findByBookTitleContaining(@Param("title") String title);

  @Modifying
  @Transactional
  @Query(
      "UPDATE BookFormat bf SET bf.price = :price WHERE bf.book.id = :bookId AND bf.format.id ="
          + " :formatId")
  int updatePrice(
      @Param("bookId") UUID bookId,
      @Param("formatId") UUID formatId,
      @Param("price") BigDecimal price);

  @Modifying
  @Transactional
  void deleteByBookIdAndFormatId(UUID bookId, UUID formatId);

  @Modifying
  @Transactional
  void deleteByBookId(UUID bookId);

  @Modifying
  @Transactional
  void deleteByFormatId(UUID formatId);
}
