package com.example.demo.librairie.service;

import com.example.demo.librairie.dto.GenreRequest;
import com.example.demo.librairie.dto.GenreResponse;
import com.example.demo.librairie.entity.Genre;
import com.example.demo.librairie.repository.GenreRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenreService {

  private final GenreRepository genreRepository;

  public List<GenreResponse> getAll() {
    return genreRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
  }

  public GenreResponse getById(UUID id) {
    Genre genre =
        genreRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Genre not found with id: " + id));
    return toResponse(genre);
  }

  public Genre getGenreEntityById(UUID id) {
    return genreRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Genre not found with id: " + id));
  }

  public List<Genre> getGenresByIds(List<UUID> ids) {
    return genreRepository.findAllById(ids);
  }

  public GenreResponse create(GenreRequest request) {
    Genre genre =
        Genre.builder().name(request.getName()).description(request.getDescription()).build();
    return toResponse(genreRepository.save(genre));
  }

  public GenreResponse update(UUID id, GenreRequest request) {
    Genre genre =
        genreRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Genre not found with id: " + id));

    genre.setName(request.getName());
    genre.setDescription(request.getDescription());

    return toResponse(genreRepository.save(genre));
  }

  public void delete(UUID id) {
    if (!genreRepository.existsById(id)) {
      throw new EntityNotFoundException("Genre not found with id: " + id);
    }
    genreRepository.deleteById(id);
  }

  private GenreResponse toResponse(Genre genre) {
    return GenreResponse.builder()
        .id(genre.getId())
        .name(genre.getName())
        .description(genre.getDescription())
        .build();
  }
}
