package com.example.demo.librairie.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.librairie.dto.GenreRequest;
import com.example.demo.librairie.dto.GenreResponse;
import com.example.demo.librairie.entity.Genre;
import com.example.demo.librairie.repository.GenreRepository;
import com.example.demo.librairie.service.GenreService;
import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;
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
    genre =
        Genre.builder()
            .id(genreId)
            .name("Science-Fiction")
            .description("Livres de science-fiction")
            .build();

    genreRequest = GenreRequest.builder().name("Fantasy").description("Livres de fantasy").build();
  }

  @Test
  void getAll_ShouldReturnAllGenres() {
    List<Genre> genres = Arrays.asList(genre, new Genre());
    when(genreRepository.findAll()).thenReturn(genres);

    List<GenreResponse> result = genreService.getAll();

    assertNotNull(result);
    assertEquals(2, result.size());
    verify(genreRepository, times(1)).findAll();
  }

  @Test
  void getById_WhenGenreExists_ShouldReturnGenre() {
    when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));

    GenreResponse result = genreService.getById(genreId);

    assertNotNull(result);
    assertEquals(genreId, result.getId());
    assertEquals(genre.getName(), result.getName());
    verify(genreRepository, times(1)).findById(genreId);
  }

  @Test
  void getById_WhenGenreDoesNotExist_ShouldThrowException() {
    when(genreRepository.findById(genreId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> genreService.getById(genreId));
    verify(genreRepository, times(1)).findById(genreId);
  }

  @Test
  void create_ShouldSaveAndReturnGenre() {
    // Créer un genre qui correspond à la requête
    Genre savedGenre =
        Genre.builder()
            .id(genreId)
            .name(genreRequest.getName())
            .description(genreRequest.getDescription())
            .build();

    when(genreRepository.save(any(Genre.class))).thenReturn(savedGenre);

    GenreResponse result = genreService.create(genreRequest);

    assertNotNull(result);
    assertEquals(genreId, result.getId());
    assertEquals(genreRequest.getName(), result.getName());
    assertEquals(genreRequest.getDescription(), result.getDescription());
    verify(genreRepository, times(1)).save(any(Genre.class));
  }

  @Test
  void update_WhenGenreExists_ShouldUpdateAndReturn() {
    when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
    when(genreRepository.save(any(Genre.class))).thenReturn(genre);

    GenreResponse result = genreService.update(genreId, genreRequest);

    assertNotNull(result);
    assertEquals(genreRequest.getName(), result.getName());
    assertEquals(genreRequest.getDescription(), result.getDescription());
    verify(genreRepository, times(1)).findById(genreId);
    verify(genreRepository, times(1)).save(any(Genre.class));
  }

  @Test
  void update_WhenGenreDoesNotExist_ShouldThrowException() {
    when(genreRepository.findById(genreId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> genreService.update(genreId, genreRequest));
    verify(genreRepository, times(1)).findById(genreId);
    verify(genreRepository, never()).save(any(Genre.class));
  }

  @Test
  void delete_WhenGenreExists_ShouldDelete() {
    when(genreRepository.existsById(genreId)).thenReturn(true);
    doNothing().when(genreRepository).deleteById(genreId);

    genreService.delete(genreId);

    verify(genreRepository, times(1)).existsById(genreId);
    verify(genreRepository, times(1)).deleteById(genreId);
  }

  @Test
  void delete_WhenGenreDoesNotExist_ShouldThrowException() {
    when(genreRepository.existsById(genreId)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> genreService.delete(genreId));
    verify(genreRepository, times(1)).existsById(genreId);
    verify(genreRepository, never()).deleteById(genreId);
  }
}
