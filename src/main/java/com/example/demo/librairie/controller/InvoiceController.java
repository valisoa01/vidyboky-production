package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.InvoiceResponse;
import com.example.demo.librairie.service.InvoiceService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/orders/{orderId}/invoice")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable UUID orderId) {
        return ResponseEntity.ok(invoiceService.getOrCreateDownloadLink(orderId));
    }

    @GetMapping("/customers/{customerId}/invoices")
    public ResponseEntity<List<InvoiceResponse>> getCustomerInvoices(
            @PathVariable UUID customerId) {
        return ResponseEntity.ok(invoiceService.listByCustomer(customerId));
    }
}