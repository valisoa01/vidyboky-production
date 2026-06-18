package com.example.demo.librairie.service;

import com.example.demo.librairie.dto.FormatRequest;
import com.example.demo.librairie.dto.FormatResponse;
import com.example.demo.librairie.entity.Format;
import com.example.demo.librairie.repository.FormatRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FormatService {

  private final FormatRepository formatRepository;

  public List<FormatResponse> getAll() {
    return formatRepository.findAll().stream().map(this::toResponse).toList();
  }

  public FormatResponse getById(UUID id) {
    return toResponse(
        formatRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Format not found: " + id)));
  }

  public FormatResponse create(FormatRequest request) {
    if (formatRepository.existsByFormatType(request.getFormatType())) {
      throw new IllegalArgumentException("Format already exists: " + request.getFormatType());
    }
    Format format = Format.builder().formatType(request.getFormatType()).build();
    return toResponse(formatRepository.save(format));
  }

  public FormatResponse update(UUID id, FormatRequest request) {
    Format format =
        formatRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Format not found: " + id));
    format.setFormatType(request.getFormatType());
    return toResponse(formatRepository.save(format));
  }

  public void delete(UUID id) {
    if (!formatRepository.existsById(id)) {
      throw new EntityNotFoundException("Format not found: " + id);
    }
    formatRepository.deleteById(id);
  }

  private FormatResponse toResponse(Format format) {
    return FormatResponse.builder().id(format.getId()).formatType(format.getFormatType()).build();
  }
}
