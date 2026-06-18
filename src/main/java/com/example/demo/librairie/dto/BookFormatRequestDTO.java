package com.example.demo.librairie.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookFormatRequestDTO {
  private UUID bookId;
  private UUID formatId;
  private Double price;
}
