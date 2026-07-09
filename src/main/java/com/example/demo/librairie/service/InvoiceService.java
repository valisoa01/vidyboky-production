package com.example.demo.librairie.service;

import com.example.demo.file.bucket.BucketComponent;
import com.example.demo.librairie.dto.InvoiceResponse;
import com.example.demo.librairie.entity.Invoice;
import com.example.demo.librairie.entity.Order;
import com.example.demo.librairie.exception.ResourceNotFoundException;
import com.example.demo.librairie.repository.InvoiceRepository;
import com.example.demo.librairie.repository.OrderRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private static final Duration LINK_EXPIRATION = Duration.ofMinutes(15);
    private static final String BUCKET_PREFIX = "invoices/";

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final InvoicePdfGenerator pdfGenerator;
    private final BucketComponent bucketComponent;

    public InvoiceResponse getOrCreateDownloadLink(UUID orderId) {
        Invoice invoice = generateAndStore(orderId);
        return toResponse(invoice);
    }

    public Invoice generateAndStore(UUID orderId) {
        return invoiceRepository.findByOrderId(orderId).orElseGet(() -> createInvoice(orderId));
    }

    public List<InvoiceResponse> listByCustomer(UUID customerId) {
        return invoiceRepository.findByOrder_Customer_IdOrderByGenerationDateDesc(customerId).stream()
                .map(this::toResponse)
                .toList();
    }

    private Invoice createInvoice(UUID orderId) {
        Order order =
                orderRepository
                        .findDetailedById(orderId)
                        .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        log.info("Generating invoice PDF for order {}", orderId);
        var pdfFile = pdfGenerator.generate(order);

        var bucketKey = BUCKET_PREFIX + orderId + ".pdf";
        bucketComponent.upload(pdfFile, bucketKey);
        log.info("Invoice PDF for order {} uploaded to bucket key {}", orderId, bucketKey);

        var invoice =
                Invoice.builder().order(order).bucketKey(bucketKey).generationDate(LocalDateTime.now()).build();

        return invoiceRepository.save(invoice);
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        var downloadUrl = bucketComponent.presign(invoice.getBucketKey(), LINK_EXPIRATION);
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .orderId(invoice.getOrder().getId())
                .generationDate(invoice.getGenerationDate())
                .downloadUrl(downloadUrl.toString())
                .expiresInSeconds(LINK_EXPIRATION.toSeconds())
                .build();
    }
}