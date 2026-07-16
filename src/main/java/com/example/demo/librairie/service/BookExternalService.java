package com.example.demo.librairie.service;

import com.example.demo.librairie.entity.Author;
import com.example.demo.librairie.entity.Book;
import com.example.demo.librairie.repository.AuthorRepository;
import com.example.demo.librairie.repository.BookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookExternalService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${books.api.url}")
    private String baseApiUrl;

    /**
     * Retrieves a book by its ISBN.
     * First checks the local database. If absent, fetches from Open Library,
     * saves it locally, and returns the persisted book.
     */
    @Transactional
    public Book getOrFetchByIsbn(String isbn) {
        log.info("Searching for book with ISBN: {}", isbn);

        Optional<Book> localBook = bookRepository.findByIsbn(isbn);
        if (localBook.isPresent()) {
            log.info("Book found in local database for ISBN: {}", isbn);
            return localBook.get();
        }

        log.info("Book not found locally. Querying Open Library API for ISBN: {}", isbn);
        return fetchFromExternalApi(isbn);
    }

    private Book fetchFromExternalApi(String isbn) {
        String url = String.format("%s?bibkeys=ISBN:%s&format=json&jscmd=data", baseApiUrl, isbn);
        log.info("Calling external API: {}", url);

        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null || jsonResponse.trim().isEmpty() || "{}".equals(jsonResponse.trim())) {
                log.warn("Open Library returned an empty response for ISBN: {}", isbn);
                throw new RuntimeException("Book not found with ISBN: " + isbn);
            }

            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            String bookKey = "ISBN:" + isbn;

            if (!rootNode.has(bookKey)) {
                log.warn("No book entry found for key {} in JSON response", bookKey);
                throw new RuntimeException("Book not found with ISBN: " + isbn);
            }

            JsonNode bookNode = rootNode.get(bookKey);
            log.info("Book successfully retrieved from Open Library. Starting mapping...");

            return mapToBook(bookNode, isbn);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch book from external API for ISBN: " + isbn, e);
            throw new RuntimeException("An error occurred while fetching the book from the external service.");
        }
    }

    private Book mapToBook(JsonNode bookNode, String isbn) {
        String title = bookNode.has("title") ? bookNode.get("title").asText() : "Unknown Title";
        String url = bookNode.has("url") ? bookNode.get("url").asText() : null;

        String description = null;
        if (bookNode.has("description")) {
            JsonNode descriptionNode = bookNode.get("description");
            description = descriptionNode.isObject() ? descriptionNode.get("value").asText() : descriptionNode.asText();

            if (description != null && description.length() > 255) {
                description = description.substring(0, 252) + "...";
            }
        }

        LocalDate publicationDate = null;
        if (bookNode.has("publish_date")) {
            String rawDate = bookNode.get("publish_date").asText();
            publicationDate = parseRobustDate(rawDate);
        }

        List<Author> authors = new ArrayList<>();
        if (bookNode.has("authors")) {
            for (JsonNode authorNode : bookNode.get("authors")) {
                if (authorNode.has("name")) {
                    String fullName = authorNode.get("name").asText();
                    Author author = findOrCreateAuthor(fullName);
                    authors.add(author);
                }
            }
        }

        Book book = Book.builder()
                .title(title)
                .isbn(isbn)
                .description(description)
                .url(url)
                .creationDate(LocalDate.now())
                .publicationDate(publicationDate)
                .authors(authors)
                .genres(new ArrayList<>())
                .build();

        Book savedBook = bookRepository.save(book);
        log.info("Book '{}' successfully saved in database with ID: {}", title, savedBook.getId());

        return savedBook;
    }

    private Author findOrCreateAuthor(String fullName) {
        return authorRepository.findByFullName(fullName)
                .orElseGet(() -> {
                    log.info("Author '{}' not found in database. Creating a new entry.", fullName);
                    Author newAuthor = Author.builder()
                            .fullName(fullName)
                            .build();
                    return authorRepository.save(newAuthor);
                });
    }

    private LocalDate parseRobustDate(String rawDate) {
        try {
            return LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH));
        } catch (Exception e1) {
            try {
                return LocalDate.parse(rawDate);
            } catch (Exception e2) {
                try {
                    int year = Integer.parseInt(rawDate.replaceAll("[^0-9]", ""));
                    return LocalDate.of(year, 1, 1);
                } catch (Exception e3) {
                    log.warn("Could not parse publication date: {}. Setting to null.", rawDate);
                    return null;
                }
            }
        }
    }
}