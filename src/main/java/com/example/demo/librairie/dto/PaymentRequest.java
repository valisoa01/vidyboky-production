package com.example.demo.librairie.dto;

import com.example.demo.librairie.entity.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

  @NotNull(message = "Order ID is required")
  private UUID orderId;

  @NotNull(message = "Payment type is required")
  private PaymentType paymentType;

  @NotNull(message = "Amount is required")
  @Positive(message = "Amount must be greater than 0")
  private Double amount;

  private LocalDateTime paymentDate;
}
