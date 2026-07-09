package com.example.demo.librairie.dto;

import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookStockResponse {

    private UUID bookFormatId;
    private String formatType;
    private Integer currentStock;
}
