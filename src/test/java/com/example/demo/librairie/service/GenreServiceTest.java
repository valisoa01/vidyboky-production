package com.example.demo.librairie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.librairie.dto.GenreRequest;
import com.example.demo.librairie.dto.GenreResponse;
import com.example.demo.librairie.entity.Genre;
import com.example.demo.librairie.repository.GenreRepository;
import jakarta.persistence.EntityNotFoundException;
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
class GenreServiceTest {

  @Mock private GenreRepository genreRepository;

  @InjectMocks private GenreService genreService;

  private UUID genreId;
  private Genre genre;
  private GenreRequest genreRequest;

  @BeforeEach
  void setUp() {
    genreId = UUID.randomUUID();

    genre = Genre.builder().id(genreId).name("Fiction").description("Fiction books").build();

    genreRequest = GenreRequest.builder().name("Fiction").description("Fiction books").build();
  }

  @Test
  void getAll() {
    when(genreRepository.findAll()).thenReturn(List.of(genre));

    List<GenreResponse> result = genreService.getAll();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(genre.getName(), result.get(0).getName());
    verify(genreRepository, times(1)).findAll();
  }

  @Test
  void getById() {
    when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));

    GenreResponse result = genreService.getById(genreId);

    assertNotNull(result);
    assertEquals(genre.getName(), result.getName());
    verify(genreRepository, times(1)).findById(genreId);
  }

  @Test
  void getById_ShouldThrowException_WhenNotFound() {
    when(genreRepository.findById(genreId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> genreService.getById(genreId));
    verify(genreRepository, times(1)).findById(genreId);
  }

  @Test
  void getGenreEntityById() {
    when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));

    Genre result = genreService.getGenreEntityById(genreId);

    assertNotNull(result);
    assertEquals(genre.getName(), result.getName());
    verify(genreRepository, times(1)).findById(genreId);
  }

  @Test
  void getGenresByIds() {
    List<UUID> ids = List.of(genreId);
    when(genreRepository.findAllById(ids)).thenReturn(List.of(genre));

    List<Genre> result = genreService.getGenresByIds(ids);

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(genreRepository, times(1)).findAllById(ids);
  }

  @Test
  void create() {
    when(genreRepository.save(any(Genre.class))).thenReturn(genre);

    GenreResponse result = genreService.create(genreRequest);

    assertNotNull(result);
    assertEquals(genreRequest.getName(), result.getName());
    verify(genreRepository, times(1)).save(any(Genre.class));
  }

  @Test
  void update() {
    GenreRequest updateRequest =
        GenreRequest.builder().name("Non-Fiction").description("Non-Fiction books").build();

    Genre updatedGenre =
        Genre.builder().id(genreId).name("Non-Fiction").description("Non-Fiction books").build();

    when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
    when(genreRepository.save(any(Genre.class))).thenReturn(updatedGenre);

    GenreResponse result = genreService.update(genreId, updateRequest);

    assertNotNull(result);
    assertEquals(updateRequest.getName(), result.getName());
    verify(genreRepository, times(1)).findById(genreId);
    verify(genreRepository, times(1)).save(any(Genre.class));
  }

  @Test
  void update_ShouldThrowException_WhenNotFound() {
    when(genreRepository.findById(genreId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> genreService.update(genreId, genreRequest));
    verify(genreRepository, times(1)).findById(genreId);
    verify(genreRepository, never()).save(any(Genre.class));
  }

  @Test
  void delete() {
    when(genreRepository.existsById(genreId)).thenReturn(true);

    assertDoesNotThrow(() -> genreService.delete(genreId));
    verify(genreRepository, times(1)).existsById(genreId);
    verify(genreRepository, times(1)).deleteById(genreId);
  }

  @Test
  void delete_ShouldThrowException_WhenNotFound() {
    when(genreRepository.existsById(genreId)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> genreService.delete(genreId));
    verify(genreRepository, times(1)).existsById(genreId);
    verify(genreRepository, never()).deleteById(genreId);
  }
}
