package com.example.demo.librairie.dto;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class OpenLibraryResponseTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private static final String SAMPLE_BOOK_NODE =
      """
      {
        "title": "Zen Speaks",
        "subtitle": "Shouts of Nothingness",
        "url": "https://openlibrary.org/books/OL1397864M/Zen_speaks",
        "publish_date": "1994",
        "authors": [
          { "name": "Tsai Chih Chung", "url": "https://openlibrary.org/authors/OL545126A" }
        ],
        "publishers": [ { "name": "Anchor Books" } ],
        "number_of_pages": 168,
        "cover": { "small": "https://covers.openlibrary.org/b/id/240726-S.jpg" }
      }
      """;

  @Test
  void shouldMapKnownFieldsAndIgnoreUnknownOnes() throws Exception {
    OpenLibraryResponse response =
        objectMapper.readValue(SAMPLE_BOOK_NODE, OpenLibraryResponse.class);

    assertEquals("Zen Speaks", response.getTitle());
    assertEquals("1994", response.getPublishDate());
    assertEquals("https://openlibrary.org/books/OL1397864M/Zen_speaks", response.getUrl());
    assertEquals(1, response.getAuthors().size());
    assertEquals("Tsai Chih Chung", response.getAuthors().get(0).getName());
  }

  @Test
  void shouldMapFromRawBibkeyWrappedResponse() throws Exception {
    String raw = "{ \"ISBN:0385472579\": " + SAMPLE_BOOK_NODE + " }";

    JsonNode root = objectMapper.readTree(raw);
    JsonNode bookNode = root.get("ISBN:0385472579");

    assertNotNull(bookNode, "Le bookNode doit être trouvé pour la clé bibkey");

    OpenLibraryResponse response = objectMapper.treeToValue(bookNode, OpenLibraryResponse.class);

    assertEquals("Zen Speaks", response.getTitle());
    assertEquals("Tsai Chih Chung", response.getAuthors().get(0).getName());
  }

  @Test
  void shouldHandleMissingOptionalFieldsGracefully() throws Exception {
    String minimal = "{ \"title\": \"Untitled Work\" }";

    OpenLibraryResponse response = objectMapper.readValue(minimal, OpenLibraryResponse.class);

    assertEquals("Untitled Work", response.getTitle());
    assertNull(response.getAuthors());
    assertNull(response.getPublishDate());
  }
}
