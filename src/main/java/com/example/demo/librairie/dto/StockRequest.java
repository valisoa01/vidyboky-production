package com.example.demo.librairie.dto;

import com.example.demo.librairie.entity.MovementType;
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
public class StockRequest {

  @NotNull(message = "Book format ID is required")
  private UUID bookFormatId;

  @NotNull(message = "Movement type is required (IN or OUT)")
  private MovementType movement;

  @NotNull(message = "Quantity is required")
  @Positive(message = "Quantity must be greater than 0")
  private Integer quantity;
}
