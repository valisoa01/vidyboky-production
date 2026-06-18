package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.AuthorResponse;
import com.example.demo.librairie.dto.BookRequest;
import com.example.demo.librairie.dto.BookResponse;
import com.example.demo.librairie.dto.GenreResponse;
import com.example.demo.librairie.entity.Book;
import com.example.demo.librairie.service.BookService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

  private final BookService bookService;

  @GetMapping
  public ResponseEntity<List<BookResponse>> getAll() {
    List<Book> books = bookService.getAll();
    List<BookResponse> responses =
        books.stream().map(this::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<BookResponse> getById(@PathVariable UUID id) {
    try {
      Book book = bookService.getById(id);
      return ResponseEntity.ok(toResponse(book));
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/search/title")
  public ResponseEntity<List<BookResponse>> getByTitle(@RequestParam String title) {
    List<Book> books = bookService.getLivreByTitle(title);
    List<BookResponse> responses =
        books.stream().map(this::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/search/gender/{genderId}")
  public ResponseEntity<List<BookResponse>> getByGenre(@PathVariable("genderId") UUID genreId) {
    List<Book> books = bookService.getLivreByGenre(genreId);
    List<BookResponse> responses =
        books.stream().map(this::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/search/date")
  public ResponseEntity<List<BookResponse>> getByDate(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    List<Book> books = bookService.getLivreByDate(date);
    List<BookResponse> responses =
        books.stream().map(this::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @PostMapping
  public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
    Book book = bookService.createLivre(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(book));
  }

  @PutMapping("/{id}")
  public ResponseEntity<BookResponse> update(
      @PathVariable UUID id, @Valid @RequestBody BookRequest request) {
    Book book = bookService.updateLivre(id, request);
    return ResponseEntity.ok(toResponse(book));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    bookService.deleteLivre(id);
    return ResponseEntity.noContent().build();
  }

  // Méthode de conversion Book -> BookResponse
  private BookResponse toResponse(Book book) {
    // Conversion des auteurs
    List<AuthorResponse> authorResponses = null;
    if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
      authorResponses =
          book.getAuthors().stream()
              .map(
                  author ->
                      AuthorResponse.builder()
                          .id(author.getId())
                          .fullName(author.getFullName())
                          .firstname(author.getFirstname())
                          .lastname(author.getLastname())
                          .birthDate(author.getBirthDate())
                          .build())
              .collect(Collectors.toList());
    }

    // Conversion des genres
    List<GenreResponse> genreResponses = null;
    if (book.getGenres() != null && !book.getGenres().isEmpty()) {
      genreResponses =
          book.getGenres().stream()
              .map(
                  genre ->
                      GenreResponse.builder()
                          .id(genre.getId())
                          .name(genre.getName())
                          .description(genre.getDescription())
                          .build())
              .collect(Collectors.toList());
    }

    return BookResponse.builder()
        .id(book.getId())
        .title(book.getTitle())
        .isbn(book.getIsbn())
        .description(book.getDescription())
        .url(book.getUrl())
        .creationDate(book.getCreationDate())
        .publicationDate(book.getPublicationDate())
        .authors(authorResponses)
        .genres(genreResponses)
        .build();
  }
}
