package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.BookFormatRequestDTO;
import com.example.demo.librairie.dto.BookFormatResponseDTO;
import com.example.demo.librairie.service.BookFormatService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book-formats")
@RequiredArgsConstructor
public class BookFormatController {

  private final BookFormatService bookFormatService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BookFormatResponseDTO create(@Valid @RequestBody BookFormatRequestDTO request) {
    return bookFormatService.create(request);
  }

  @GetMapping
  public List<BookFormatResponseDTO> findAll() {
    return bookFormatService.findAll();
  }

  @GetMapping("/{id}")
  public BookFormatResponseDTO findById(@PathVariable UUID id) {
    return bookFormatService.findById(id);
  }

  @PutMapping("/{id}")
  public BookFormatResponseDTO update(
      @PathVariable UUID id, @Valid @RequestBody BookFormatRequestDTO request) {
    return bookFormatService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    bookFormatService.delete(id);
  }
}
