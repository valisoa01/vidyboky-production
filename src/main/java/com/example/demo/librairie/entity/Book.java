package com.example.demo.librairie.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "book")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "title", length = 100, nullable = false)
  private String title;

  @Column(name = "isbn", length = 100, unique = true)
  private String isbn;

  @Column(name = "description", length = 255)
  private String description;

  @Column(name = "url", length = 100)
  private String url;

  @Column(name = "creation_date")
  private LocalDate creationDate;

  @Column(name = "publication_date")
  private LocalDate publicationDate;

  @ManyToMany
  @JoinTable(
      name = "book_genre",
      joinColumns = @JoinColumn(name = "book_id"),
      inverseJoinColumns = @JoinColumn(name = "genre_id"))
  @JsonIgnore
  private List<Genre> genres;

  @ManyToMany
  @JoinTable(
      name = "book_author",
      joinColumns = @JoinColumn(name = "book_id"),
      inverseJoinColumns = @JoinColumn(name = "author_id"))
  @JsonIgnore
  private List<Author> authors;

  @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
  @JsonIgnore
  private List<BookFormat> formats;
}
