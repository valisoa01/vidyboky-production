package com.example.demo.librairie.dto;

import lombok.*;

import java.util.UUID;


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
