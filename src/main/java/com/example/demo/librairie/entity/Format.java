package com.example.demo.librairie.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "format")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Format {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "format_type", length = 100, nullable = false)
  private String formatType;

  @OneToMany(mappedBy = "format", cascade = CascadeType.ALL)
  private List<BookFormat> bookFormats;
}
