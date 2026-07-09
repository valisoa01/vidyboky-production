package com.example.demo.librairie.dto;

import com.example.demo.librairie.entity.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

  @NotNull(message = "Customer ID is required")
  private UUID customerId;

  @NotNull(message = "Order type is required")
  private OrderType orderType;

  private LocalDateTime orderDate;

  @NotEmpty(message = "An order must contain at least one line")
  @Valid
  private List<OrderLineRequest> lines;
}
