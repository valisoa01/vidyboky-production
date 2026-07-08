package com.example.demo.librairie.dto;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenreRevenueResponse {

  private UUID genreId;
  private String genreName;
  private Double totalRevenue;
}
