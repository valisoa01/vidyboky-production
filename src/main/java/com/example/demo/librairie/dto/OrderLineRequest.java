package com.example.demo.librairie.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class OrderLineRequest {

  @NotNull private UUID orderId;

  @NotNull private UUID bookFormatId;

  @NotNull @Positive private Integer quantity;
}
