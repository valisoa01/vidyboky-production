package com.example.demo.librairie.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "delivery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "address", length = 255, nullable = false)
  private String address;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private DeliveryStatus status;

  @Column(name = "expected_date")
  private LocalDate expectedDate;

  @Column(name = "effective_date")
  private LocalDate effectiveDate;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false, unique = true)
  private Order order;
}
