package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.BookResponse;
import com.example.demo.librairie.dto.BookRequest;
import com.example.demo.librairie.entity.Book;
import com.example.demo.librairie.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID bookId;
    private Book book;
    private BookRequest bookRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        bookId = UUID.randomUUID();
        book = new Book();
        book.setId(bookId);
        book.setTitle("Test Book");
        book.setIsbn("978-1234567890");
        book.setPublicationDate(LocalDate.of(2023, 1, 15));

        bookRequest = new BookRequest();
        bookRequest.setTitle("Test Book");
        bookRequest.setIsbn("978-1234567890");
        bookRequest.setPublicationDate(LocalDate.of(2023, 1, 15));
    }
}