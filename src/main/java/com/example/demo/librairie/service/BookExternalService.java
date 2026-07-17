package com.example.demo.librairie.service;

import com.example.demo.librairie.dto.GoogleBooksResponse;
import com.example.demo.librairie.dto.OpenLibraryResponse;
import com.example.demo.librairie.entity.Author;
import com.example.demo.librairie.entity.Book;
import com.example.demo.librairie.exception.ExternalServiceException;
import com.example.demo.librairie.exception.ResourceNotFoundException;
import com.example.demo.librairie.repository.AuthorRepository;
import com.example.demo.librairie.repository.BookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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

  @Value("${google.books.api.url}")
  private String googleBooksApiUrl;

  /**
   * Retrieves a book by its ISBN. First checks the local database. If absent, queries Open Library
   * and Google Books in parallel-ish fashion (sequentially, each failure-tolerant), merges the
   * results (Open Library as primary source, Google Books filling in any gaps), saves it locally,
   * and returns the persisted book.
   */
  @Transactional
  public Book getOrFetchByIsbn(String isbn) {
    if (!StringUtils.hasText(isbn)) {
      throw new IllegalArgumentException("ISBN must not be blank");
    }

    log.info("Searching for book with ISBN: {}", isbn);

    Optional<Book> localBook = bookRepository.findByIsbn(isbn);
    if (localBook.isPresent()) {
      log.info("Book found in local database for ISBN: {}", isbn);
      return localBook.get();
    }

    log.info("Book not found locally. Querying external APIs for ISBN: {}", isbn);
    return fetchFromExternalApis(isbn);
  }

  private Book fetchFromExternalApis(String isbn) {
    boolean openLibraryFailed = false;
    boolean googleBooksFailed = false;

    OpenLibraryResponse openLibraryBook = null;
    try {
      openLibraryBook = fetchFromOpenLibrary(isbn);
    } catch (ExternalServiceException e) {
      log.error("Open Library technical failure for ISBN: {}", isbn, e);
      openLibraryFailed = true;
    }

    GoogleBooksResponse.VolumeInfo googleBook = null;
    try {
      googleBook = fetchFromGoogleBooks(isbn);
    } catch (ExternalServiceException e) {
      log.error("Google Books technical failure for ISBN: {}", isbn, e);
      googleBooksFailed = true;
    }

    if (openLibraryBook == null && googleBook == null) {
      if (openLibraryFailed && googleBooksFailed) {
        // Aucun des deux fournisseurs n'a répondu correctement : c'est une panne technique,
        // pas une absence de donnée. On ne doit pas dire au client "livre introuvable" alors
        // qu'on n'a en réalité même pas pu vérifier.
        throw new ExternalServiceException(
            "Open Library / Google Books",
            "both providers are unreachable or failed for ISBN " + isbn);
      }
      log.warn("Book not found on Open Library nor Google Books for ISBN: {}", isbn);
      throw new ResourceNotFoundException("Book", "ISBN", isbn);
    }

    return mapToBook(openLibraryBook, googleBook, isbn);
  }

  private OpenLibraryResponse fetchFromOpenLibrary(String isbn) {
    String url = String.format("%s?bibkeys=ISBN:%s&format=json&jscmd=data", baseApiUrl, isbn);
    log.info("Calling Open Library: {}", url);

    String jsonResponse;
    try {
      jsonResponse = restTemplate.getForObject(url, String.class);
    } catch (ResourceAccessException e) {
      // Timeout, DNS, connexion refusée... la faute vient du réseau/service, pas de l'ISBN.
      throw new ExternalServiceException("Open Library", "network error", e);
    } catch (HttpServerErrorException e) {
      // 5xx : le service est en panne côté fournisseur.
      throw new ExternalServiceException("Open Library", "server error " + e.getStatusCode(), e);
    } catch (HttpClientErrorException e) {
      // 4xx inattendu (ex: 429 rate-limit, 400 malformé) : problème d'intégration, pas d'ISBN.
      throw new ExternalServiceException("Open Library", "client error " + e.getStatusCode(), e);
    } catch (RestClientException e) {
      throw new ExternalServiceException("Open Library", "call failed", e);
    }

    try {
      if (jsonResponse == null
          || jsonResponse.trim().isEmpty()
          || "{}".equals(jsonResponse.trim())) {
        log.warn("Open Library returned an empty response for ISBN: {}", isbn);
        return null;
      }

      JsonNode rootNode = objectMapper.readTree(jsonResponse);
      String bookKey = "ISBN:" + isbn;

      if (!rootNode.has(bookKey)) {
        log.warn("No book entry found for key {} in Open Library response", bookKey);
        return null;
      }

      return objectMapper.treeToValue(rootNode.get(bookKey), OpenLibraryResponse.class);

    } catch (Exception e) {
      // Réponse reçue mais illisible : c'est un vrai problème d'intégration (format inattendu),
      // pas une absence de donnée pour cet ISBN.
      throw new ExternalServiceException("Open Library", "unreadable response", e);
    }
  }

  private GoogleBooksResponse.VolumeInfo fetchFromGoogleBooks(String isbn) {
    String url = String.format("%s?q=isbn:%s", googleBooksApiUrl, isbn);
    log.info("Calling Google Books: {}", url);

    GoogleBooksResponse response;
    try {
      response = restTemplate.getForObject(url, GoogleBooksResponse.class);
    } catch (ResourceAccessException e) {
      throw new ExternalServiceException("Google Books", "network error", e);
    } catch (HttpServerErrorException e) {
      throw new ExternalServiceException("Google Books", "server error " + e.getStatusCode(), e);
    } catch (HttpClientErrorException e) {
      throw new ExternalServiceException("Google Books", "client error " + e.getStatusCode(), e);
    } catch (RestClientException e) {
      throw new ExternalServiceException("Google Books", "call failed", e);
    }

    if (response == null
        || response.getItems() == null
        || response.getItems().isEmpty()
        || response.getItems().get(0).getVolumeInfo() == null) {
      log.warn("Google Books returned no result for ISBN: {}", isbn);
      return null;
    }

    return response.getItems().get(0).getVolumeInfo();
  }

  private Book mapToBook(
      OpenLibraryResponse openLibraryBook, GoogleBooksResponse.VolumeInfo googleBook, String isbn) {

    String title =
        firstNonBlank(
            openLibraryBook != null ? openLibraryBook.getTitle() : null,
            googleBook != null ? googleBook.getTitle() : null,
            "Unknown Title");

    String url =
        firstNonBlank(
            openLibraryBook != null ? openLibraryBook.getUrl() : null,
            googleBook != null ? googleBook.getInfoLink() : null,
            null);

    String description =
        firstNonBlank(
            openLibraryBook != null ? openLibraryBook.getDescriptionText() : null,
            googleBook != null ? googleBook.getDescription() : null,
            null);
    if (description != null && description.length() > 255) {
      description = description.substring(0, 252) + "...";
    }

    LocalDate publicationDate = null;
    String rawOpenLibraryDate = openLibraryBook != null ? openLibraryBook.getPublishDate() : null;
    String rawGoogleDate = googleBook != null ? googleBook.getPublishedDate() : null;
    String rawDate = firstNonBlank(rawOpenLibraryDate, rawGoogleDate, null);
    if (rawDate != null) {
      publicationDate = parseRobustDate(rawDate);
    }

    List<Author> authors = new ArrayList<>();
    if (openLibraryBook != null
        && openLibraryBook.getAuthors() != null
        && !openLibraryBook.getAuthors().isEmpty()) {
      for (OpenLibraryResponse.Author authorNode : openLibraryBook.getAuthors()) {
        if (StringUtils.hasText(authorNode.getName())) {
          authors.add(findOrCreateAuthor(authorNode.getName()));
        }
      }
    } else if (googleBook != null && googleBook.getAuthors() != null) {
      for (String authorName : googleBook.getAuthors()) {
        if (StringUtils.hasText(authorName)) {
          authors.add(findOrCreateAuthor(authorName));
        }
      }
    }

    Book book =
        Book.builder()
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

  private String firstNonBlank(String a, String b, String fallback) {
    if (StringUtils.hasText(a)) {
      return a;
    }
    if (StringUtils.hasText(b)) {
      return b;
    }
    return fallback;
  }

  private Author findOrCreateAuthor(String fullName) {
    return authorRepository
        .findByFullName(fullName)
        .orElseGet(
            () -> {
              log.info("Author '{}' not found in database. Creating a new entry.", fullName);
              Author newAuthor = Author.builder().fullName(fullName).build();
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
