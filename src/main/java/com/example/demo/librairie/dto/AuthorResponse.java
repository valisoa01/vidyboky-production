package com.example.demo.librairie.dto;

import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorResponse {

  private UUID id;
  private String fullName;
  private String firstname;
  private String lastname;
  private LocalDate birthDate;
}
