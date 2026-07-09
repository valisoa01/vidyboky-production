package com.example.demo.librairie.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.demo.librairie.dto.BookFormatRequestDTO;
import com.example.demo.librairie.dto.BookFormatResponseDTO;
import com.example.demo.librairie.exception.ResourceNotFoundException;
import com.example.demo.librairie.service.BookFormatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(BookFormatController.class)
class BookFormatControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private BookFormatService bookFormatService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void create() throws Exception {
    UUID bookId = UUID.randomUUID();
    UUID formatId = UUID.randomUUID();

    BookFormatRequestDTO request =
        BookFormatRequestDTO.builder().bookId(bookId).formatId(formatId).price(29.99).build();

    BookFormatResponseDTO response =
        BookFormatResponseDTO.builder()
            .id(UUID.randomUUID())
            .bookId(bookId)
            .bookTitle("Sample Book")
            .formatId(formatId)
            .typeFormat("Paperback")
            .price(29.99)
            .build();

    when(bookFormatService.create(any(BookFormatRequestDTO.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/book-formats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.bookId").value(bookId.toString()))
        .andExpect(jsonPath("$.bookTitle").value("Sample Book"))
        .andExpect(jsonPath("$.formatId").value(formatId.toString()))
        .andExpect(jsonPath("$.typeFormat").value("Paperback"))
        .andExpect(jsonPath("$.price").value(29.99));

    verify(bookFormatService, times(1)).create(any(BookFormatRequestDTO.class));
  }

  @Test
  void findAll() throws Exception {
    UUID bookId1 = UUID.randomUUID();
    UUID formatId1 = UUID.randomUUID();
    UUID bookId2 = UUID.randomUUID();
    UUID formatId2 = UUID.randomUUID();

    List<BookFormatResponseDTO> bookFormats =
        Arrays.asList(
            BookFormatResponseDTO.builder()
                .id(UUID.randomUUID())
                .bookId(bookId1)
                .bookTitle("Book One")
                .formatId(formatId1)
                .typeFormat("Hardcover")
                .price(39.99)
                .build(),
            BookFormatResponseDTO.builder()
                .id(UUID.randomUUID())
                .bookId(bookId2)
                .bookTitle("Book Two")
                .formatId(formatId2)
                .typeFormat("E-book")
                .price(19.99)
                .build());

    when(bookFormatService.findAll()).thenReturn(bookFormats);

    mockMvc
        .perform(get("/book-formats").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].bookTitle").value("Book One"))
        .andExpect(jsonPath("$[1].bookTitle").value("Book Two"));

    verify(bookFormatService, times(1)).findAll();
  }

  @Test
  void findById() throws Exception {
    UUID id = UUID.randomUUID();
    UUID bookId = UUID.randomUUID();
    UUID formatId = UUID.randomUUID();

    BookFormatResponseDTO response =
        BookFormatResponseDTO.builder()
            .id(id)
            .bookId(bookId)
            .bookTitle("Found Book")
            .formatId(formatId)
            .typeFormat("Paperback")
            .price(24.99)
            .build();

    when(bookFormatService.findById(id)).thenReturn(response);

    mockMvc
        .perform(get("/book-formats/{id}", id).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.bookTitle").value("Found Book"))
        .andExpect(jsonPath("$.typeFormat").value("Paperback"))
        .andExpect(jsonPath("$.price").value(24.99));

    verify(bookFormatService, times(1)).findById(id);
  }

  @Test
  void update() throws Exception {
    UUID id = UUID.randomUUID();
    UUID bookId = UUID.randomUUID();
    UUID formatId = UUID.randomUUID();

    BookFormatRequestDTO request =
        BookFormatRequestDTO.builder().bookId(bookId).formatId(formatId).price(34.99).build();

    BookFormatResponseDTO response =
        BookFormatResponseDTO.builder()
            .id(id)
            .bookId(bookId)
            .bookTitle("Updated Book")
            .formatId(formatId)
            .typeFormat("Hardcover")
            .price(34.99)
            .build();

    when(bookFormatService.update(eq(id), any(BookFormatRequestDTO.class))).thenReturn(response);

    mockMvc
        .perform(
            put("/book-formats/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.bookTitle").value("Updated Book"))
        .andExpect(jsonPath("$.price").value(34.99));

    verify(bookFormatService, times(1)).update(eq(id), any(BookFormatRequestDTO.class));
  }

  @Test
  void delete() throws Exception {
    UUID id = UUID.randomUUID();
    doNothing().when(bookFormatService).delete(id);

    mockMvc
        .perform(MockMvcRequestBuilders.delete("/book-formats/{id}", id))
        .andExpect(status().isNoContent());

    verify(bookFormatService, times(1)).delete(id);
  }

  @Test
  void create_WithInvalidData_ShouldReturnBadRequest() throws Exception {
    BookFormatRequestDTO request =
        BookFormatRequestDTO.builder().bookId(null).formatId(null).price(null).build();

    mockMvc
        .perform(
            post("/book-formats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.validationErrors").exists())
        .andExpect(jsonPath("$.validationErrors.bookId").value("Book ID is required"))
        .andExpect(jsonPath("$.validationErrors.formatId").value("Format ID is required"))
        .andExpect(jsonPath("$.validationErrors.price").value("Price is required"));

    verify(bookFormatService, never()).create(any(BookFormatRequestDTO.class));
  }

  @Test
  void findById_NotFound_ShouldReturn404() throws Exception {
    UUID id = UUID.randomUUID();

    when(bookFormatService.findById(id))
        .thenThrow(new ResourceNotFoundException("BookFormat", "id", id.toString()));

    mockMvc
        .perform(get("/book-formats/{id}", id))
        .andDo(
            result -> {
              System.out.println("Status: " + result.getResponse().getStatus());
              System.out.println("Response body: " + result.getResponse().getContentAsString());
            })
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").exists());
  }
}
