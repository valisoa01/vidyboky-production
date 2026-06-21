package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.AuthorRequest;
import com.example.demo.librairie.dto.AuthorResponse;
import com.example.demo.librairie.exception.DuplicateResourceException;
import com.example.demo.librairie.exception.ResourceNotFoundException;
import com.example.demo.librairie.service.AuthorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthorController.class)
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthorService authorService;

    private UUID authorId;
    private AuthorRequest authorRequest;
    private AuthorResponse authorResponse;
    private List<AuthorResponse> authorResponseList;

    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();

        authorRequest = AuthorRequest.builder()
                .fullName("Victor Hugo")
                .firstname("Victor")
                .lastname("Hugo")
                .birthDate(LocalDate.of(1802, 2, 26))
                .build();

        authorResponse = AuthorResponse.builder()
                .id(authorId)
                .fullName("Victor Hugo")
                .firstname("Victor")
                .lastname("Hugo")
                .birthDate(LocalDate.of(1802, 2, 26))
                .build();

        AuthorResponse authorResponse2 = AuthorResponse.builder()
                .id(UUID.randomUUID())
                .fullName("Albert Camus")
                .firstname("Albert")
                .lastname("Camus")
                .birthDate(LocalDate.of(1913, 11, 7))
                .build();

        authorResponseList = List.of(authorResponse, authorResponse2);
    }

    @Test
    void getAll_ShouldReturnListOfAuthors() throws Exception {
        when(authorService.getAll()).thenReturn(authorResponseList);

        mockMvc.perform(get("/authors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(authorId.toString()))
                .andExpect(jsonPath("$[0].fullName").value("Victor Hugo"))
                .andExpect(jsonPath("$[0].firstname").value("Victor"))
                .andExpect(jsonPath("$[0].lastname").value("Hugo"))
                .andExpect(jsonPath("$[0].birthDate").value("1802-02-26"))
                .andExpect(jsonPath("$[1].fullName").value("Albert Camus"))
                .andExpect(jsonPath("$[1].firstname").value("Albert"))
                .andExpect(jsonPath("$[1].lastname").value("Camus"))
                .andExpect(jsonPath("$[1].birthDate").value("1913-11-07"));

        verify(authorService).getAll();
    }

    @Test
    void getById_WithValidId_ShouldReturnAuthor() throws Exception {
        when(authorService.getById(authorId)).thenReturn(authorResponse);

        mockMvc.perform(get("/authors/{id}", authorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(authorId.toString()))
                .andExpect(jsonPath("$.fullName").value("Victor Hugo"))
                .andExpect(jsonPath("$.firstname").value("Victor"))
                .andExpect(jsonPath("$.lastname").value("Hugo"))
                .andExpect(jsonPath("$.birthDate").value("1802-02-26"));

        verify(authorService).getById(authorId);
    }

    @Test
    void getById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        UUID invalidId = UUID.randomUUID();
        when(authorService.getById(invalidId))
                .thenThrow(new ResourceNotFoundException("Author", invalidId));


        mockMvc.perform(get("/authors/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(authorService).getById(invalidId);
    }

    @Test
    void create_WithValidRequest_ShouldReturnCreatedAuthor() throws Exception {
        when(authorService.create(any(AuthorRequest.class))).thenReturn(authorResponse);

        mockMvc.perform(post("/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(authorId.toString()))
                .andExpect(jsonPath("$.fullName").value("Victor Hugo"))
                .andExpect(jsonPath("$.firstname").value("Victor"))
                .andExpect(jsonPath("$.lastname").value("Hugo"))
                .andExpect(jsonPath("$.birthDate").value("1802-02-26"));

        verify(authorService).create(any(AuthorRequest.class));
    }

    @Test
    void create_WithDuplicateFullName_ShouldReturnConflict() throws Exception {
        when(authorService.create(any(AuthorRequest.class)))
                .thenThrow(new DuplicateResourceException("Author", "fullName", "Victor Hugo"));

        mockMvc.perform(post("/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorRequest)))
                .andExpect(status().isConflict());

        verify(authorService).create(any(AuthorRequest.class));
    }

    @Test
    void create_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        AuthorRequest invalidRequest = AuthorRequest.builder()
                .fullName("")
                .firstname("Victor")
                .lastname("Hugo")
                .birthDate(LocalDate.of(1802, 2, 26))
                .build();

        mockMvc.perform(post("/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_WithValidIdAndRequest_ShouldReturnUpdatedAuthor() throws Exception {
        AuthorResponse updatedResponse = AuthorResponse.builder()
                .id(authorId)
                .fullName("Victor-Marie Hugo")
                .firstname("Victor-Marie")
                .lastname("Hugo")
                .birthDate(LocalDate.of(1802, 2, 26))
                .build();

        when(authorService.update(eq(authorId), any(AuthorRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/authors/{id}", authorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(authorId.toString()))
                .andExpect(jsonPath("$.fullName").value("Victor-Marie Hugo"))
                .andExpect(jsonPath("$.firstname").value("Victor-Marie"))
                .andExpect(jsonPath("$.lastname").value("Hugo"))
                .andExpect(jsonPath("$.birthDate").value("1802-02-26"));

        verify(authorService).update(eq(authorId), any(AuthorRequest.class));
    }

    @Test
    void update_WithInvalidId_ShouldReturnNotFound() throws Exception {
        UUID invalidId = UUID.randomUUID();
        when(authorService.update(eq(invalidId), any(AuthorRequest.class)))
                .thenThrow(new ResourceNotFoundException("Author", invalidId));

        mockMvc.perform(put("/authors/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorRequest)))
                .andExpect(status().isNotFound());

        verify(authorService).update(eq(invalidId), any(AuthorRequest.class));
    }

    @Test
    void update_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        AuthorRequest invalidRequest = AuthorRequest.builder()
                .fullName("a".repeat(101))
                .firstname("Victor")
                .lastname("Hugo")
                .birthDate(LocalDate.of(1802, 2, 26))
                .build();

        mockMvc.perform(put("/authors/{id}", authorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_WithValidId_ShouldReturnNoContent() throws Exception {
        doNothing().when(authorService).delete(authorId);

        mockMvc.perform(delete("/authors/{id}", authorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(authorService).delete(authorId);
    }

    @Test
    void delete_WithInvalidId_ShouldReturnNotFound() throws Exception {
        UUID invalidId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Author", invalidId))
                .when(authorService).delete(invalidId);

        mockMvc.perform(delete("/authors/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(authorService).delete(invalidId);
    }
}