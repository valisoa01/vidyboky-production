package com.example.demo.librairie.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "book_format")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookFormat {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "price", nullable = false)
  private Double price;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_id", nullable = false)
  private Book book;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "format_id", nullable = false)
  private Format format;

  @OneToMany(mappedBy = "bookFormat", cascade = CascadeType.ALL)
  private List<Stock> stocks;
}
