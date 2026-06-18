package com.example.demo.librairie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.librairie.dto.AuthorRequest;
import com.example.demo.librairie.dto.AuthorResponse;
import com.example.demo.librairie.entity.Author;
import com.example.demo.librairie.exception.DuplicateResourceException;
import com.example.demo.librairie.exception.ResourceNotFoundException;
import com.example.demo.librairie.repository.AuthorRepository;
import java.time.LocalDate;
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
            .birthDate(LocalDate.of(1990, 1, 1))
            .build();

    authorRequest = new AuthorRequest();
    authorRequest.setFullName("John Doe");
    authorRequest.setFirstname("John");
    authorRequest.setLastname("Doe");
    authorRequest.setBirthDate(LocalDate.of(1990, 1, 1));
  }

  // ✅ CORRECTED: Expect ResourceNotFoundException instead of EntityNotFoundException
  @Test
  void getById_ShouldThrowException_WhenNotFound() {
    // Arrange
    when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> authorService.getById(authorId));
  }

  // ✅ CORRECTED: Expect DuplicateResourceException instead of IllegalArgumentException
  @Test
  void create_ShouldThrowException_WhenAuthorAlreadyExists() {
    // Arrange
    when(authorRepository.existsByFullName(authorRequest.getFullName())).thenReturn(true);

    // Act & Assert
    assertThrows(DuplicateResourceException.class, () -> authorService.create(authorRequest));
  }

  @Test
  void create_ShouldReturnAuthorResponse_WhenSuccessful() {
    // Arrange
    when(authorRepository.existsByFullName(authorRequest.getFullName())).thenReturn(false);
    when(authorRepository.save(any(Author.class))).thenReturn(author);

    // Act
    AuthorResponse response = authorService.create(authorRequest);

    // Assert
    assertNotNull(response);
    assertEquals(authorId, response.getId());
    assertEquals("John Doe", response.getFullName());
    verify(authorRepository).save(any(Author.class));
  }

  @Test
  void getAll_ShouldReturnListOfAuthorResponses() {
    // Arrange
    when(authorRepository.findAll()).thenReturn(java.util.List.of(author));

    // Act
    var responses = authorService.getAll();

    // Assert
    assertNotNull(responses);
    assertEquals(1, responses.size());
    assertEquals(authorId, responses.get(0).getId());
  }

  @Test
  void getById_ShouldReturnAuthorResponse_WhenFound() {
    // Arrange
    when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));

    // Act
    AuthorResponse response = authorService.getById(authorId);

    // Assert
    assertNotNull(response);
    assertEquals(authorId, response.getId());
    assertEquals("John Doe", response.getFullName());
  }

  // ✅ CORRECTED: Expect ResourceNotFoundException instead of EntityNotFoundException
  @Test
  void update_ShouldThrowException_WhenNotFound() {
    // Arrange
    when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class, () -> authorService.update(authorId, authorRequest));
  }

  @Test
  void update_ShouldReturnUpdatedAuthorResponse_WhenFound() {
    // Arrange
    Author updatedAuthor =
        Author.builder()
            .id(authorId)
            .fullName("Jane Doe")
            .firstname("Jane")
            .lastname("Doe")
            .birthDate(LocalDate.of(1995, 1, 1))
            .build();

    when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
    when(authorRepository.save(any(Author.class))).thenReturn(updatedAuthor);

    // Act
    AuthorResponse response = authorService.update(authorId, authorRequest);

    // Assert
    assertNotNull(response);
    assertEquals(authorId, response.getId());
    verify(authorRepository).save(any(Author.class));
  }

  // ✅ CORRECTED: Expect ResourceNotFoundException instead of EntityNotFoundException
  @Test
  void delete_ShouldThrowException_WhenNotFound() {
    // Arrange
    when(authorRepository.existsById(authorId)).thenReturn(false);

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> authorService.delete(authorId));
  }

  @Test
  void delete_ShouldDeleteAuthor_WhenFound() {
    // Arrange
    when(authorRepository.existsById(authorId)).thenReturn(true);
    doNothing().when(authorRepository).deleteById(authorId);

    // Act
    authorService.delete(authorId);

    // Assert
    verify(authorRepository).deleteById(authorId);
  }
}
