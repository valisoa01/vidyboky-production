package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.GenreRequest;
import com.example.demo.librairie.dto.GenreResponse;
import com.example.demo.librairie.service.GenreService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

  private final GenreService genreService;

  @GetMapping
  public ResponseEntity<List<GenreResponse>> getAll() {
    return ResponseEntity.ok(genreService.getAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<GenreResponse> getById(@PathVariable UUID id) {
    try {
      return ResponseEntity.ok(genreService.getById(id));
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping
  public ResponseEntity<GenreResponse> create(@Valid @RequestBody GenreRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(genreService.create(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<GenreResponse> update(
      @PathVariable UUID id, @Valid @RequestBody GenreRequest request) {
    return ResponseEntity.ok(genreService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    genreService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
