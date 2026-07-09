package com.example.demo.librairie.exception;

import java.util.UUID;

public class InvoiceGenerationException extends RuntimeException {

    public InvoiceGenerationException(UUID orderId, Throwable cause) {
        super("Failed to generate invoice PDF for order id: " + orderId, cause);
    }
}