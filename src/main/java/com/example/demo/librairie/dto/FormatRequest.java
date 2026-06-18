package com.example.demo.librairie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormatRequest {

  @NotBlank(message = "Format type is required")
  @Size(max = 100)
  private String formatType;
}
