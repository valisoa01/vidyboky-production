package com.example.demo.librairie.dto;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLineResponse {

  private UUID id;
  private UUID bookFormatId;
  private String bookTitle;
  private String formatType;
  private Integer quantity;
  private Double unitPrice;
  private Double lineTotal;
}
