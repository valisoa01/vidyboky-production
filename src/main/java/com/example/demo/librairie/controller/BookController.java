package com.example.demo.librairie.controller;

import com.example.demo.librairie.dto.BookRequest;
import com.example.demo.librairie.dto.BookResponse;
import com.example.demo.librairie.entity.Book;
import com.example.demo.librairie.service.BookExternalService;
import com.example.demo.librairie.service.BookService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

  private final BookService bookService;
  private final BookExternalService bookExternalService;

  @GetMapping
  public ResponseEntity<List<BookResponse>> getAll() {

    List<BookResponse> responses =
        bookService.getAll().stream().map(bookService::toBookResponse).toList();

    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{id}")
  public BookResponse getById(@PathVariable UUID id) {
    return bookService.toBookResponse(bookService.getById(id));
  }

  // ======= NOUVEL ENDPOINT =======
  @GetMapping("/isbn/{isbn}")
  public BookResponse getByIsbn(@PathVariable String isbn) {

    Book book = bookExternalService.getOrFetchByIsbn(isbn);

    return bookService.toBookResponse(book);
  }

  @GetMapping("/searchByTitle")
  public ResponseEntity<List<BookResponse>> getByTitle(@RequestParam String title) {

    List<BookResponse> responses =
        bookService.getLivreByTitle(title).stream().map(bookService::toBookResponse).toList();

    return ResponseEntity.ok(responses);
  }

  @GetMapping("/search/gender/{genderId}")
  public ResponseEntity<List<BookResponse>> getByGenre(@PathVariable("genderId") UUID genreId) {

    List<BookResponse> responses =
        bookService.getLivreByGenre(genreId).stream().map(bookService::toBookResponse).toList();

    return ResponseEntity.ok(responses);
  }

  @GetMapping("/searchByDate")
  public ResponseEntity<List<BookResponse>> getByDate(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

    List<BookResponse> responses =
        bookService.getLivreByDate(date).stream().map(bookService::toBookResponse).toList();

    return ResponseEntity.ok(responses);
  }

  @PostMapping
  public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {

    Book created = bookService.createLivre(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(bookService.toBookResponse(created));
  }

  @PutMapping("/{id}")
  public ResponseEntity<BookResponse> update(
      @PathVariable UUID id, @Valid @RequestBody BookRequest request) {

    Book updated = bookService.updateLivre(id, request);

    return ResponseEntity.ok(bookService.toBookResponse(updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {

    bookService.deleteLivre(id);

    return ResponseEntity.noContent().build();
  }
}
