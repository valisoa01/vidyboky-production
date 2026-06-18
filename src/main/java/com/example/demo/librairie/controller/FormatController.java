package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.FormatRequest;
import com.example.demo.librairie.dto.FormatResponse;
import com.example.demo.librairie.service.FormatService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/formats")
@RequiredArgsConstructor
public class FormatController {

  private final FormatService formatService;

  @GetMapping
  public List<FormatResponse> getAll() {
    return formatService.getAll();
  }

  @GetMapping("/{id}")
  public FormatResponse getById(@PathVariable UUID id) {
    return formatService.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public FormatResponse create(@Valid @RequestBody FormatRequest request) {
    return formatService.create(request);
  }

  @PutMapping("/{id}")
  public FormatResponse update(@PathVariable UUID id, @Valid @RequestBody FormatRequest request) {
    return formatService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    formatService.delete(id);
  }
}
