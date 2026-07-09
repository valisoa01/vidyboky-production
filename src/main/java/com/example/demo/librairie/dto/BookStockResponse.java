package com.example.demo.librairie.dto;

import java.util.UUID;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookStockResponse {

  private UUID bookFormatId;
  private String formatType;
  private Integer currentStock;
}
