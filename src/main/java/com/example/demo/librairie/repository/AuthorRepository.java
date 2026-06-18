package com.example.demo.librairie.repository;

import com.example.demo.librairie.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthorRepository extends JpaRepository<Author, UUID> {

  Optional<Author> findByFullName(String fullName);

  boolean existsByFullName(String fullName);
}
