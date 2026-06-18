package com.example.demo.librairie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.librairie.dto.FormatRequest;
import com.example.demo.librairie.dto.FormatResponse;
import com.example.demo.librairie.entity.Format;
import com.example.demo.librairie.repository.FormatRepository;
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
class FormatServiceTest {

  @Mock private FormatRepository formatRepository;

  @InjectMocks private FormatService formatService;

  private UUID formatId;
  private Format format;
  private FormatRequest formatRequest;

  @BeforeEach
  void setUp() {
    formatId = UUID.randomUUID();

    format = Format.builder().id(formatId).formatType("Paperback").build();

    formatRequest = FormatRequest.builder().formatType("Paperback").build();
  }

  @Test
  void getAll_ShouldReturnListOfFormats() {
    when(formatRepository.findAll()).thenReturn(List.of(format));

    List<FormatResponse> result = formatService.getAll();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(format.getFormatType(), result.get(0).getFormatType());
    verify(formatRepository, times(1)).findAll();
  }

  @Test
  void getAll_ShouldReturnEmptyList_WhenNoFormats() {
    when(formatRepository.findAll()).thenReturn(List.of());

    List<FormatResponse> result = formatService.getAll();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(formatRepository, times(1)).findAll();
  }

  @Test
  void getById_ShouldReturnFormat_WhenExists() {
    when(formatRepository.findById(formatId)).thenReturn(Optional.of(format));

    FormatResponse result = formatService.getById(formatId);

    assertNotNull(result);
    assertEquals(format.getFormatType(), result.getFormatType());
    verify(formatRepository, times(1)).findById(formatId);
  }

  @Test
  void getById_ShouldThrowException_WhenNotFound() {
    when(formatRepository.findById(formatId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> formatService.getById(formatId));
    verify(formatRepository, times(1)).findById(formatId);
  }

  @Test
  void create_ShouldReturnCreatedFormat_WhenValidRequest() {
    when(formatRepository.existsByFormatType(anyString())).thenReturn(false);
    when(formatRepository.save(any(Format.class))).thenReturn(format);

    FormatResponse result = formatService.create(formatRequest);

    assertNotNull(result);
    assertEquals(formatRequest.getFormatType(), result.getFormatType());
    verify(formatRepository, times(1)).existsByFormatType(anyString());
    verify(formatRepository, times(1)).save(any(Format.class));
  }

  @Test
  void create_ShouldThrowException_WhenFormatAlreadyExists() {
    when(formatRepository.existsByFormatType(anyString())).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> formatService.create(formatRequest));
    verify(formatRepository, times(1)).existsByFormatType(anyString());
    verify(formatRepository, never()).save(any(Format.class));
  }

  @Test
  void update_ShouldReturnUpdatedFormat_WhenExists() {
    FormatRequest updateRequest = FormatRequest.builder().formatType("Hardcover").build();

    Format updatedFormat = Format.builder().id(formatId).formatType("Hardcover").build();

    when(formatRepository.findById(formatId)).thenReturn(Optional.of(format));
    when(formatRepository.save(any(Format.class))).thenReturn(updatedFormat);

    FormatResponse result = formatService.update(formatId, updateRequest);

    assertNotNull(result);
    assertEquals(updateRequest.getFormatType(), result.getFormatType());
    verify(formatRepository, times(1)).findById(formatId);
    verify(formatRepository, times(1)).save(any(Format.class));
  }

  @Test
  void update_ShouldThrowException_WhenNotFound() {
    when(formatRepository.findById(formatId)).thenReturn(Optional.empty());

    assertThrows(
        EntityNotFoundException.class, () -> formatService.update(formatId, formatRequest));
    verify(formatRepository, times(1)).findById(formatId);
    verify(formatRepository, never()).save(any(Format.class));
  }

  @Test
  void delete_ShouldDeleteFormat_WhenExists() {
    when(formatRepository.existsById(formatId)).thenReturn(true);

    assertDoesNotThrow(() -> formatService.delete(formatId));
    verify(formatRepository, times(1)).existsById(formatId);
    verify(formatRepository, times(1)).deleteById(formatId);
  }

  @Test
  void delete_ShouldThrowException_WhenNotFound() {
    when(formatRepository.existsById(formatId)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> formatService.delete(formatId));
    verify(formatRepository, times(1)).existsById(formatId);
    verify(formatRepository, never()).deleteById(formatId);
  }
}
