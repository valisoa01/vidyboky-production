package com.example.demo.librairie.dto;

import com.example.demo.librairie.entity.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {
    private UUID id;
    private MovementType movement;
    private Integer quantity;
    private LocalDateTime movementDate;
    private UUID bookFormatId;
    private String bookTitle;
    private String formatType;
}