package com.example.demo.librairie.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
  @NotNull(message = "Book ID is required")
  private UUID bookId;

  @NotNull(message = "Format ID is required")
  private UUID formatId;

  @NotNull(message = "Price is required")
  @Positive(message = "Price must be greater than 0")
  private Double price;
}
