package com.example.demo.librairie.dto;

import com.example.demo.librairie.entity.OrderType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

  private UUID id;
  private OrderType orderType;
  private LocalDateTime orderDate;
  private UUID customerId;
  private String customerFullName;
  private List<OrderLineResponse> lines;
  private Double totalAmount;
}
