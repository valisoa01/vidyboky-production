package com.example.demo.librairie.dto;

import com.example.demo.librairie.entity.DeliveryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRequest {

  @NotBlank(message = "Address is required")
  @Size(max = 255)
  private String address;

  @NotNull(message = "Status is required")
  private DeliveryStatus status;

  @NotNull(message = "Expected delivery date is required")
  private LocalDate expectedDate;

  private LocalDate effectiveDate;

  @NotNull(message = "Order id is required")
  private UUID orderId;
}
