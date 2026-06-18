package com.example.demo.librairie.service;

import com.example.demo.librairie.dto.AuthorRequest;
import com.example.demo.librairie.dto.AuthorResponse;
import com.example.demo.librairie.entity.Author;
import com.example.demo.librairie.repository.AuthorRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorService {

  private final AuthorRepository authorRepository;

  public List<AuthorResponse> getAll() {
    return authorRepository.findAll().stream().map(this::toResponse).toList();
  }

  public AuthorResponse getById(UUID id) {
    Author author =
        authorRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Author not found: " + id));
    return toResponse(author);
  }

  public AuthorResponse create(AuthorRequest request) {
    if (authorRepository.existsByFullName(request.getFullName())) {
      throw new IllegalArgumentException("Author already exists: " + request.getFullName());
    }
    Author author =
        Author.builder()
            .fullName(request.getFullName())
            .firstname(request.getFirstname())
            .lastname(request.getLastname())
            .birthDate(request.getBirthDate())
            .build();
    return toResponse(authorRepository.save(author));
  }

  public AuthorResponse update(UUID id, AuthorRequest request) {
    Author author =
        authorRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Author not found: " + id));
    author.setFullName(request.getFullName());
    author.setFirstname(request.getFirstname());
    author.setLastname(request.getLastname());
    author.setBirthDate(request.getBirthDate());
    return toResponse(authorRepository.save(author));
  }

  public void delete(UUID id) {
    if (!authorRepository.existsById(id)) {
      throw new EntityNotFoundException("Author not found: " + id);
    }
    authorRepository.deleteById(id);
  }

  private AuthorResponse toResponse(Author author) {
    return AuthorResponse.builder()
        .id(author.getId())
        .fullName(author.getFullName())
        .firstname(author.getFirstname())
        .lastname(author.getLastname())
        .birthDate(author.getBirthDate())
        .build();
  }
}
