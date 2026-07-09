package com.example.demo.librairie.controller;

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

import com.example.demo.librairie.dto.BookRequest;
import com.example.demo.librairie.entity.Author;
import com.example.demo.librairie.entity.Book;
import com.example.demo.librairie.entity.Genre;
import com.example.demo.librairie.exception.ResourceNotFoundException;
import com.example.demo.librairie.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
class BookControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private BookService bookService;

  private UUID bookId;
  private UUID genreId;
  private UUID authorId;
  private BookRequest bookRequest;
  private Book bookWithRelations;
  private Book bookWithoutRelations;

  @BeforeEach
  void setUp() {
    bookId = UUID.randomUUID();
    genreId = UUID.randomUUID();
    authorId = UUID.randomUUID();

    bookRequest =
        BookRequest.builder()
            .title("Pride and Prejudice")
            .isbn("978-0141439518")
            .description("Un classique de la romance")
            .url("https://example.com/book/pride")
            .publicationDate(LocalDate.of(1813, 1, 28))
            .genreIds(List.of(genreId))
            .authorIds(List.of(authorId))
            .build();

    Author author =
        Author.builder()
            .id(authorId)
            .fullName("Jane Austen")
            .firstname("Jane")
            .lastname("Austen")
            .birthDate(LocalDate.of(1775, 12, 16))
            .build();

    Genre genre =
        Genre.builder().id(genreId).name("Romance").description("Livres de romance").build();

    bookWithRelations =
        Book.builder()
            .id(bookId)
            .title("Pride and Prejudice")
            .isbn("978-0141439518")
            .description("Un classique de la romance")
            .url("https://example.com/book/pride")
            .creationDate(LocalDate.of(2024, 1, 1))
            .publicationDate(LocalDate.of(1813, 1, 28))
            .genres(List.of(genre))
            .authors(List.of(author))
            .build();

    bookWithoutRelations =
        Book.builder()
            .id(UUID.randomUUID())
            .title("Livre sans relations")
            .isbn("000-0000000000")
            .description("Aucun auteur ni genre")
            .url("https://example.com/book/none")
            .creationDate(LocalDate.of(2024, 1, 1))
            .publicationDate(LocalDate.of(2020, 5, 10))
            .genres(Collections.emptyList())
            .authors(null)
            .build();
  }

  @Test
  void getAll_ShouldReturnListOfBooks() throws Exception {
    when(bookService.getAll()).thenReturn(List.of(bookWithRelations, bookWithoutRelations));

    mockMvc
        .perform(get("/books").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id").value(bookId.toString()))
        .andExpect(jsonPath("$[0].title").value("Pride and Prejudice"))
        .andExpect(jsonPath("$[0].authors", hasSize(1)))
        .andExpect(jsonPath("$[0].authors[0].fullName").value("Jane Austen"))
        .andExpect(jsonPath("$[0].genres", hasSize(1)))
        .andExpect(jsonPath("$[0].genres[0].name").value("Romance"))
        .andExpect(jsonPath("$[1].title").value("Livre sans relations"))
        .andExpect(jsonPath("$[1].authors").doesNotExist())
        .andExpect(jsonPath("$[1].genres").doesNotExist());

    verify(bookService).getAll();
  }

  @Test
  void getById_WithValidId_ShouldReturnBook() throws Exception {
    when(bookService.getById(bookId)).thenReturn(bookWithRelations);

    mockMvc
        .perform(get("/books/{id}", bookId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(bookId.toString()))
        .andExpect(jsonPath("$.title").value("Pride and Prejudice"))
        .andExpect(jsonPath("$.isbn").value("978-0141439518"))
        .andExpect(jsonPath("$.authors[0].firstname").value("Jane"))
        .andExpect(jsonPath("$.genres[0].description").value("Livres de romance"));

    verify(bookService).getById(bookId);
  }

  @Test
  void getById_WithBookHavingNoRelations_ShouldReturnNullLists() throws Exception {
    when(bookService.getById(bookWithoutRelations.getId())).thenReturn(bookWithoutRelations);

    mockMvc
        .perform(
            get("/books/{id}", bookWithoutRelations.getId())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Livre sans relations"))
        .andExpect(jsonPath("$.authors").doesNotExist())
        .andExpect(jsonPath("$.genres").doesNotExist());

    verify(bookService).getById(bookWithoutRelations.getId());
  }

  @Test
  void getById_WithInvalidId_ShouldReturnNotFound() throws Exception {
    UUID invalidId = UUID.randomUUID();
    when(bookService.getById(invalidId))
        .thenThrow(new ResourceNotFoundException("Book", invalidId));

    mockMvc
        .perform(get("/books/{id}", invalidId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    verify(bookService).getById(invalidId);
  }

  @Test
  void getByTitle_ShouldReturnMatchingBooks() throws Exception {
    when(bookService.getLivreByTitle("Pride")).thenReturn(List.of(bookWithRelations));

    mockMvc
        .perform(
            get("/books/searchByTitle")
                .param("title", "Pride")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].title").value("Pride and Prejudice"));

    verify(bookService).getLivreByTitle("Pride");
  }

  @Test
  void getByTitle_WithNoMatch_ShouldReturnEmptyList() throws Exception {
    when(bookService.getLivreByTitle("Inconnu")).thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            get("/books/searchByTitle")
                .param("title", "Inconnu")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));

    verify(bookService).getLivreByTitle("Inconnu");
  }

  @Test
  void getByGenre_ShouldReturnMatchingBooks() throws Exception {
    when(bookService.getLivreByGenre(genreId)).thenReturn(List.of(bookWithRelations));

    mockMvc
        .perform(
            get("/books/search/gender/{genderId}", genreId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].genres[0].id").value(genreId.toString()));

    verify(bookService).getLivreByGenre(genreId);
  }

  @Test
  void getByDate_ShouldReturnMatchingBooks() throws Exception {
    LocalDate date = LocalDate.of(1813, 1, 28);
    when(bookService.getLivreByDate(date)).thenReturn(List.of(bookWithRelations));

    mockMvc
        .perform(
            get("/books/searchByDate")
                .param("date", date.toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].publicationDate").value("1813-01-28"));

    verify(bookService).getLivreByDate(date);
  }

  @Test
  void create_WithValidRequest_ShouldReturnCreatedBook() throws Exception {
    when(bookService.createLivre(any(BookRequest.class))).thenReturn(bookWithRelations);

    mockMvc
        .perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(bookId.toString()))
        .andExpect(jsonPath("$.title").value("Pride and Prejudice"));

    verify(bookService).createLivre(any(BookRequest.class));
  }

  @Test
  void create_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
    BookRequest invalidRequest = BookRequest.builder().title("").build();

    mockMvc
        .perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void update_WithValidIdAndRequest_ShouldReturnUpdatedBook() throws Exception {
    Book updated = bookWithRelations;
    updated.setTitle("Pride and Prejudice (édition révisée)");

    when(bookService.updateLivre(eq(bookId), any(BookRequest.class))).thenReturn(updated);

    mockMvc
        .perform(
            put("/books/{id}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Pride and Prejudice (édition révisée)"));

    verify(bookService).updateLivre(eq(bookId), any(BookRequest.class));
  }

  @Test
  void update_WithInvalidId_ShouldReturnNotFound() throws Exception {
    UUID invalidId = UUID.randomUUID();
    when(bookService.updateLivre(eq(invalidId), any(BookRequest.class)))
        .thenThrow(new ResourceNotFoundException("Book", invalidId));

    mockMvc
        .perform(
            put("/books/{id}", invalidId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequest)))
        .andExpect(status().isNotFound());

    verify(bookService).updateLivre(eq(invalidId), any(BookRequest.class));
  }

  @Test
  void update_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
    BookRequest invalidRequest = BookRequest.builder().title("a".repeat(101)).build();

    mockMvc
        .perform(
            put("/books/{id}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void delete_WithValidId_ShouldReturnNoContent() throws Exception {
    doNothing().when(bookService).deleteLivre(bookId);

    mockMvc
        .perform(delete("/books/{id}", bookId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    verify(bookService).deleteLivre(bookId);
  }

  @Test
  void delete_WithInvalidId_ShouldReturnNotFound() throws Exception {
    UUID invalidId = UUID.randomUUID();
    doThrow(new ResourceNotFoundException("Book", invalidId))
        .when(bookService)
        .deleteLivre(invalidId);

    mockMvc
        .perform(delete("/books/{id}", invalidId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    verify(bookService).deleteLivre(invalidId);
  }
}
