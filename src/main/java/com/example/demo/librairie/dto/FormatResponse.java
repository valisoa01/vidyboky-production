package com.example.demo.librairie.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormatResponse {

  private UUID id;
  private String formatType;
}
