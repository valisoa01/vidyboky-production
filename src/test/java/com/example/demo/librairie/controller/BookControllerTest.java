package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.BookResponse;
import com.example.demo.librairie.dto.BookRequest;
import com.example.demo.librairie.entity.Book;
import com.example.demo.librairie.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
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
    private LocalDate publicationDate;
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        publicationDate = LocalDate.of(2023, 1, 15);

        bookId = UUID.randomUUID();
        book = new Book();
        book.setId(bookId);
        book.setTitle("Test Book");
        book.setIsbn("978-1234567890");
        book.setDescription("Test Description");
        book.setUrl("http://test.com");
        book.setCreationDate(LocalDate.now());
        book.setPublicationDate(publicationDate);
    }
    @Test
    void getAll_ShouldReturnListOfBooks() throws Exception {

        List<Book> books = Arrays.asList(book);
        when(bookService.getAll()).thenReturn(books);

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(bookId.toString()))
                .andExpect(jsonPath("$[0].title").value("Test Book"))
                .andExpect(jsonPath("$[0].isbn").value("978-1234567890"))
                .andExpect(jsonPath("$[0].description").value("Test Description"))
                .andExpect(jsonPath("$[0].url").value("http://test.com"));
        verify(bookService, times(1)).getAll();
    }
}