package com.example.demo.librairie.service;

import com.example.demo.librairie.dto.AuthorResponse;
import com.example.demo.librairie.dto.BookRequest;
import com.example.demo.librairie.dto.BookResponse;
import com.example.demo.librairie.dto.GenreResponse;
import com.example.demo.librairie.entity.Author;
import com.example.demo.librairie.entity.Book;
import com.example.demo.librairie.entity.Genre;
import com.example.demo.librairie.exception.ResourceNotFoundException;
import com.example.demo.librairie.repository.AuthorRepository;
import com.example.demo.librairie.repository.BookRepository;
import com.example.demo.librairie.repository.GenreRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {

  private final BookRepository bookRepository;
  private final AuthorRepository authorRepository;
  private final GenreRepository genreRepository;

  public List<Book> getAll() {
    return bookRepository.findAll();
  }

  public Book getById(UUID id) {
    return bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book", id));
  }

  public List<Book> getLivreByTitle(String title) {
    return bookRepository.findByTitleContainingIgnoreCase(title);
  }

  public List<Book> getLivreByGenre(UUID genreId) {
    return bookRepository.findByGenres_Id(genreId);
  }

  public List<Book> getLivreByDate(LocalDate date) {
    return bookRepository.findByPublicationDate(date);
  }

  public Book createLivre(BookRequest request) {
    Book book = new Book();
    book.setTitle(request.getTitle());
    book.setIsbn(request.getIsbn());
    book.setDescription(request.getDescription());
    book.setUrl(request.getUrl());
    book.setPublicationDate(request.getPublicationDate());
    book.setCreationDate(LocalDate.now());

    if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
      List<Genre> genres = genreRepository.findAllById(request.getGenreIds());
      book.setGenres(genres);
    }

    if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
      List<Author> authors = authorRepository.findAllById(request.getAuthorIds());
      book.setAuthors(authors);
    }

    return bookRepository.save(book);
  }

  public Book updateLivre(UUID id, BookRequest request) {
    Book book =
        bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book", id));

    book.setTitle(request.getTitle());
    book.setIsbn(request.getIsbn());
    book.setDescription(request.getDescription());
    book.setUrl(request.getUrl());
    book.setPublicationDate(request.getPublicationDate());

    if (request.getGenreIds() != null) {
      List<Genre> genres = genreRepository.findAllById(request.getGenreIds());
      book.setGenres(genres);
    }

    if (request.getAuthorIds() != null) {
      List<Author> authors = authorRepository.findAllById(request.getAuthorIds());
      book.setAuthors(authors);
    }

    return bookRepository.save(book);
  }

  public void deleteLivre(UUID id) {
    if (!bookRepository.existsById(id)) {
      throw new ResourceNotFoundException("Book", id);
    }
    bookRepository.deleteById(id);
  }

  public BookResponse toBookResponse(Book book) {

    List<AuthorResponse> authorResponses = null;

    if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {

      authorResponses =
              book.getAuthors().stream()
                      .map(author ->
                              AuthorResponse.builder()
                                      .id(author.getId())
                                      .fullName(author.getFullName())
                                      .firstname(author.getFirstname())
                                      .lastname(author.getLastname())
                                      .birthDate(author.getBirthDate())
                                      .build())
                      .toList();
    }

    List<GenreResponse> genreResponses = null;

    if (book.getGenres() != null && !book.getGenres().isEmpty()) {

      genreResponses =
              book.getGenres().stream()
                      .map(genre ->
                              GenreResponse.builder()
                                      .id(genre.getId())
                                      .name(genre.getName())
                                      .description(genre.getDescription())
                                      .build())
                      .toList();
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
