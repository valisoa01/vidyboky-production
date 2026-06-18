package com.example.demo.librairie.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "movement", nullable = false)
  private MovementType movement;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "movement_date", nullable = false)
  private LocalDateTime movementDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_format_id", nullable = false)
  private BookFormat bookFormat;
}
