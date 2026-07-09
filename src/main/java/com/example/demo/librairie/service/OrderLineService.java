package com.example.demo.librairie.service;

import com.example.demo.librairie.repository.BookFormatRepository;
import com.example.demo.librairie.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderLineService {

    private final OrderLineRepository orderLineRepository;
    private final OrderRepository orderRepository;
    private final BookFormatRepository bookFormatRepository;

}