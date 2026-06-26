package com.example.demo.librairie.dto;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequest {

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false)
  private String name;

  @Column(unique = true, nullable = false)
  private String email;

  private String phone;
}
