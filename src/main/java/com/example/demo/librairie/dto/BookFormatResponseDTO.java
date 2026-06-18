package com.example.demo.librairie.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookFormatResponseDTO {
  private UUID id;
  private UUID bookId;
  private String bookTitle;
  private UUID formatId;
  private String typeFormat;
  private Double price;
}
