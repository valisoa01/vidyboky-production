package com.example.demo.librairie.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLineRequest {

  @NotNull(message = "Book format ID is required")
  private UUID bookFormatId;

  @NotNull(message = "Quantity is required")
  @Positive(message = "Quantity must be greater than 0")
  private Integer quantity;
}
