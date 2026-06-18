package com.example.demo.librairie.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "genre")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Genre {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "name", length = 100)
  private String name;

  @Column(name = "description", length = 255)
  private String description;

  @ManyToMany(mappedBy = "genres")
  @JsonIgnore
  private List<Book> books;
}
