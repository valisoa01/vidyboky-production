package com.example.demo.librairie.service;

import com.example.demo.librairie.dto.BookFormatRequestDTO;
import com.example.demo.librairie.dto.BookFormatResponseDTO;
import com.example.demo.librairie.entity.Book;
import com.example.demo.librairie.entity.BookFormat;
import com.example.demo.librairie.entity.Format;
import com.example.demo.librairie.repository.BookFormatRepository;
import com.example.demo.librairie.repository.BookRepository;
import com.example.demo.librairie.repository.FormatRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookFormatService {

  private final BookFormatRepository bookFormatRepository;
  private final BookRepository bookRepository;
  private final FormatRepository formatRepository;

  @Transactional
  public BookFormatResponseDTO create(BookFormatRequestDTO request) {

    Book book =
        bookRepository
            .findById(request.getBookId())
            .orElseThrow(() -> new RuntimeException("Book not found"));

    Format format =
        formatRepository
            .findById(request.getFormatId())
            .orElseThrow(() -> new RuntimeException("Format not found"));

    if (bookFormatRepository.existsByBookIdAndFormatId(
        request.getBookId(), request.getFormatId())) {
      throw new RuntimeException("This book already has this format");
    }

    BookFormat bookFormat =
        BookFormat.builder().price(request.getPrice()).book(book).format(format).build();

    return toResponseDTO(bookFormatRepository.save(bookFormat));
  }

  @Transactional(readOnly = true)
  public List<BookFormatResponseDTO> findAll() {
    return bookFormatRepository.findAll().stream()
        .map(this::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public BookFormatResponseDTO findById(UUID id) {
    BookFormat bookFormat =
        bookFormatRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("BookFormat not found"));
    return toResponseDTO(bookFormat);
  }

  @Transactional
  public BookFormatResponseDTO update(UUID id, BookFormatRequestDTO request) {

    BookFormat bookFormat =
        bookFormatRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("BookFormat not found"));

    Book book =
        bookRepository
            .findById(request.getBookId())
            .orElseThrow(() -> new RuntimeException("Book not found"));

    Format format =
        formatRepository
            .findById(request.getFormatId())
            .orElseThrow(() -> new RuntimeException("Format not found"));

    bookFormat.setPrice(request.getPrice());
    bookFormat.setBook(book);
    bookFormat.setFormat(format);

    return toResponseDTO(bookFormatRepository.save(bookFormat));
  }

  @Transactional
  public void delete(UUID id) {
    if (!bookFormatRepository.existsById(id)) {
      throw new RuntimeException("BookFormat not found");
    }
    bookFormatRepository.deleteById(id);
  }

  private BookFormatResponseDTO toResponseDTO(BookFormat bookFormat) {
    return BookFormatResponseDTO.builder()
        .id(bookFormat.getId())
        .price(bookFormat.getPrice())
        .bookId(bookFormat.getBook().getId())
        .bookTitle(bookFormat.getBook().getTitle())
        .formatId(bookFormat.getFormat().getId())
        .typeFormat(bookFormat.getFormat().getFormatType())
        .build();
  }
}
