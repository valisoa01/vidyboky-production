package com.example.demo.librairie.dto;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
  private UUID id;
  private String firstName;
  private String name;
  private String email;
  private String phone;
  private String address;
}
