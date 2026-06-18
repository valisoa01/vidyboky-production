package com.example.demo.librairie.service;

import com.example.demo.librairie.dto.BookRequest;
import com.example.demo.librairie.entity.Author;
import com.example.demo.librairie.entity.Book;
import com.example.demo.librairie.entity.Genre;
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
    return bookRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
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
        bookRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

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
      throw new RuntimeException("Book not found with id: " + id);
    }
    bookRepository.deleteById(id);
  }
}
