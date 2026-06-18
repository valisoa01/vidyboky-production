package com.example.demo.librairie.dto;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenreResponse {

  private UUID id;
  private String name;
  private String description;
}
