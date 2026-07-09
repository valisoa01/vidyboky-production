package com.example.demo.librairie.dto;

import com.example.demo.librairie.entity.DeliveryStatus;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryResponse {

    private UUID id;
    private String address;
    private DeliveryStatus status;
    private LocalDate expectedDate;
    private LocalDate effectiveDate;
    private UUID orderId;
}