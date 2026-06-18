package com.example.demo.librairie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {

  @NotBlank(message = "Title is required")
  @Size(max = 100)
  private String title;

  @Size(max = 100)
  private String isbn;

  @Size(max = 255)
  private String description;

  @Size(max = 100)
  private String url;

  private LocalDate publicationDate;

  private List<UUID> genreIds;

  private List<UUID> authorIds;
}
