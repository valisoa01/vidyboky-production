package com.example.demo.librairie.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "author")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "full_name", length = 100, nullable = false)
  private String fullName;

  @Column(name = "firstname", length = 100)
  private String firstname;

  @Column(name = "lastname", length = 100)
  private String lastname;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @ManyToMany(mappedBy = "authors")
  @JsonIgnore
  private List<Book> books;
}
