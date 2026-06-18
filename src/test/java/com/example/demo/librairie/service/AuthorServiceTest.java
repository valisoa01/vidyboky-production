package com.example.demo.librairie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.librairie.dto.AuthorRequest;
import com.example.demo.librairie.dto.AuthorResponse;
import com.example.demo.librairie.entity.Author;
import com.example.demo.librairie.repository.AuthorRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

  @Mock private AuthorRepository authorRepository;

  @InjectMocks private AuthorService authorService;

  private UUID authorId;
  private Author author;
  private AuthorRequest authorRequest;

  @BeforeEach
  void setUp() {
    authorId = UUID.randomUUID();

    author =
        Author.builder()
            .id(authorId)
            .fullName("John Doe")
            .firstname("John")
            .lastname("Doe")
            .birthDate(LocalDate.of(1980, 1, 1))
            .build();

    authorRequest =
        AuthorRequest.builder()
            .fullName("John Doe")
            .firstname("John")
            .lastname("Doe")
            .birthDate(LocalDate.of(1980, 1, 1))
            .build();
  }

  @Test
  void getAll() {
    when(authorRepository.findAll()).thenReturn(List.of(author));

    List<AuthorResponse> result = authorService.getAll();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(author.getFullName(), result.get(0).getFullName());
    verify(authorRepository, times(1)).findAll();
  }

  @Test
  void getById() {
    when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));

    AuthorResponse result = authorService.getById(authorId);

    assertNotNull(result);
    assertEquals(author.getFullName(), result.getFullName());
    verify(authorRepository, times(1)).findById(authorId);
  }

  @Test
  void getById_ShouldThrowException_WhenNotFound() {
    when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> authorService.getById(authorId));
    verify(authorRepository, times(1)).findById(authorId);
  }

  @Test
  void create() {
    when(authorRepository.existsByFullName(anyString())).thenReturn(false);
    when(authorRepository.save(any(Author.class))).thenReturn(author);

    AuthorResponse result = authorService.create(authorRequest);

    assertNotNull(result);
    assertEquals(authorRequest.getFullName(), result.getFullName());
    verify(authorRepository, times(1)).existsByFullName(anyString());
    verify(authorRepository, times(1)).save(any(Author.class));
  }

  @Test
  void create_ShouldThrowException_WhenAuthorAlreadyExists() {
    when(authorRepository.existsByFullName(anyString())).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> authorService.create(authorRequest));
    verify(authorRepository, times(1)).existsByFullName(anyString());
    verify(authorRepository, never()).save(any(Author.class));
  }

  @Test
  void update() {
    AuthorRequest updateRequest =
        AuthorRequest.builder()
            .fullName("Jane Doe")
            .firstname("Jane")
            .lastname("Doe")
            .birthDate(LocalDate.of(1985, 1, 1))
            .build();

    Author updatedAuthor =
        Author.builder()
            .id(authorId)
            .fullName("Jane Doe")
            .firstname("Jane")
            .lastname("Doe")
            .birthDate(LocalDate.of(1985, 1, 1))
            .build();

    when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
    when(authorRepository.save(any(Author.class))).thenReturn(updatedAuthor);

    AuthorResponse result = authorService.update(authorId, updateRequest);

    assertNotNull(result);
    assertEquals(updateRequest.getFullName(), result.getFullName());
    verify(authorRepository, times(1)).findById(authorId);
    verify(authorRepository, times(1)).save(any(Author.class));
  }

  @Test
  void update_ShouldThrowException_WhenNotFound() {
    when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

    assertThrows(
        EntityNotFoundException.class, () -> authorService.update(authorId, authorRequest));
    verify(authorRepository, times(1)).findById(authorId);
    verify(authorRepository, never()).save(any(Author.class));
  }

  @Test
  void delete() {
    when(authorRepository.existsById(authorId)).thenReturn(true);

    assertDoesNotThrow(() -> authorService.delete(authorId));
    verify(authorRepository, times(1)).existsById(authorId);
    verify(authorRepository, times(1)).deleteById(authorId);
  }

  @Test
  void delete_ShouldThrowException_WhenNotFound() {
    when(authorRepository.existsById(authorId)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> authorService.delete(authorId));
    verify(authorRepository, times(1)).existsById(authorId);
    verify(authorRepository, never()).deleteById(authorId);
  }
}
