package com.example.demo.librairie.dto;

import com.example.demo.librairie.entity.PaymentType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

  private UUID id;
  private PaymentType paymentType;
  private Double amount;
  private LocalDateTime paymentDate;
  private UUID orderId;
}
