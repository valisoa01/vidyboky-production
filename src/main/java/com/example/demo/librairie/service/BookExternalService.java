package com.example.demo.librairie.service;

import com.example.demo.librairie.dto.BookResponse;
import com.example.demo.librairie.entity.Book;
import com.example.demo.librairie.exception.ResourceNotFoundException;
import com.example.demo.librairie.repository.BookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Iterator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class BookExternalService {

    private final BookRepository bookRepository;
    private final BookService bookService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${books.api.url}")
    private String openLibraryUrl;

    public BookResponse findByIsbn(String isbn) {

        // 1 - Chercher dans la base
        Book localBook =
                bookRepository.findByIsbn(isbn).orElse(null);

        if (localBook != null) {
            return bookService.toBookResponse(localBook);
        }

        // 2 - Chercher dans OpenLibrary
        Book apiBook = fetchFromOpenLibrary(isbn);

        if (apiBook != null) {

            Book saved = bookRepository.save(apiBook);

            return bookService.toBookResponse(saved);
        }

        // Google Books sera ajouté ensuite

        throw new ResourceNotFoundException("Book", "isbn", isbn);
    }

    private Book fetchFromOpenLibrary(String isbn) {

        try {

            String url =
                    openLibraryUrl
                            + "?bibkeys=ISBN:"
                            + isbn
                            + "&format=json&jscmd=data";

            String json = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(json);

            if (root.isEmpty()) {
                return null;
            }

            Iterator<JsonNode> iterator = root.elements();

            if (!iterator.hasNext()) {
                return null;
            }

            JsonNode node = iterator.next();

            Book book = new Book();

            book.setTitle(node.path("title").asText(null));

            book.setIsbn(isbn);

            if (node.has("publish_date")) {
                try {
                    book.setPublicationDate(
                            LocalDate.parse(node.get("publish_date").asText()));
                } catch (Exception ignored) {
                }
            }

            if (node.has("cover")) {
                book.setUrl(
                        node.path("cover")
                                .path("large")
                                .asText(null));
            }

            book.setCreationDate(LocalDate.now());

            return book;

        } catch (Exception e) {

            return null;
        }
    }
}
