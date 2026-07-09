package com.example.demo.librairie.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLineResponse {

  private UUID id;

  private UUID orderId;

  private UUID bookFormatId;

  private String bookTitle;

  private String formatType;

  private Integer quantity;

  private Double unitPrice;

  private Double totalPrice;
}
