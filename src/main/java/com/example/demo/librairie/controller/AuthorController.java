package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.AuthorRequest;
import com.example.demo.librairie.dto.AuthorResponse;
import com.example.demo.librairie.service.AuthorService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/authors")
@RequiredArgsConstructor
public class AuthorController {

  private final AuthorService authorService;

  @GetMapping
  public List<AuthorResponse> getAll() {
    return authorService.getAll();
  }

  @GetMapping("/{id}")
  public AuthorResponse getById(@PathVariable UUID id) {
    return authorService.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AuthorResponse create(@Valid @RequestBody AuthorRequest request) {
    return authorService.create(request);
  }

  @PutMapping("/{id}")
  public AuthorResponse update(@PathVariable UUID id, @Valid @RequestBody AuthorRequest request) {
    return authorService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    authorService.delete(id);
  }
}
