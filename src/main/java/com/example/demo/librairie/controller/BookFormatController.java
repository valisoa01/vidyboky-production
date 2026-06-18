package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.BookFormatRequestDTO;
import com.example.demo.librairie.dto.BookFormatResponseDTO;
import com.example.demo.librairie.service.BookFormatService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/book-formats")
@RequiredArgsConstructor
public class BookFormatController {

  private final BookFormatService bookFormatService;

  @PostMapping
  public ResponseEntity<BookFormatResponseDTO> create(@RequestBody BookFormatRequestDTO request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(bookFormatService.create(request));
  }

  @GetMapping
  public ResponseEntity<List<BookFormatResponseDTO>> findAll() {
    return ResponseEntity.ok(bookFormatService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<BookFormatResponseDTO> findById(@PathVariable UUID id) {
    return ResponseEntity.ok(bookFormatService.findById(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<BookFormatResponseDTO> update(
      @PathVariable UUID id, @RequestBody BookFormatRequestDTO request) {
    return ResponseEntity.ok(bookFormatService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    bookFormatService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
