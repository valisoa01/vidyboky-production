package com.example.demo.librairie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorRequest {

  @NotBlank(message = "Full name is required")
  @Size(max = 100)
  private String fullName;

  @Size(max = 100)
  private String firstname;

  @Size(max = 100)
  private String lastname;

  private LocalDate birthDate;
}
