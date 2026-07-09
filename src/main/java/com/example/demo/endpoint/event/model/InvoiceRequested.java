package com.example.demo.endpoint.event.model;

import java.time.Duration;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class InvoiceRequested extends PojaEvent {
    private String orderId;

    @Override
    public Duration maxConsumerDuration() {
        return Duration.ofSeconds(60);
    }

    @Override
    public Duration maxConsumerBackoffBetweenRetries() {
        return Duration.ofSeconds(30);
    }
}