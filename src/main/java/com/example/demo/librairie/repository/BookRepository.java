package com.example.demo.librairie.repository;

import com.example.demo.librairie.entity.Book;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

  List<Book> findByTitleContainingIgnoreCase(String title);

  List<Book> findByPublicationDate(LocalDate date);

  List<Book> findByAuthors_Id(UUID authorId);

  List<Book> findByGenres_Id(UUID genreId);
}
