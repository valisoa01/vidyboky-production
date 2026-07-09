package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.DeliveryRequest;
import com.example.demo.librairie.dto.DeliveryResponse;
import com.example.demo.librairie.service.DeliveryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping
    public List<DeliveryResponse> getAll() {
        return deliveryService.getAll();
    }

    @GetMapping("/{id}")
    public DeliveryResponse getById(@PathVariable UUID id) {
        return deliveryService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryResponse create(@Valid @RequestBody DeliveryRequest request) {
        return deliveryService.create(request);
    }

    @PutMapping("/{id}")
    public DeliveryResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody DeliveryRequest request) {
        return deliveryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        deliveryService.delete(id);
    }
}