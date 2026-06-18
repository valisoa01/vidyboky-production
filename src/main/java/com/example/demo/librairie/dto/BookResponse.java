package com.example.demo.librairie.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {

  private UUID id;
  private String title;
  private String isbn;
  private String description;
  private String url;
  private LocalDate creationDate;
  private LocalDate publicationDate;
  private List<AuthorResponse> authors;
  private List<GenreResponse> genres;
}
