package com.example.demo.librairie.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InvoiceResponse {
    private UUID id;
    private UUID orderId;
    private LocalDateTime generationDate;
    private String downloadUrl;
    private long expiresInSeconds;
}