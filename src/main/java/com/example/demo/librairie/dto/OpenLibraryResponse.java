package com.example.demo.librairie.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Représente le sous-objet "livre" renvoyé par l'API Open Library (endpoint:
 * https://openlibrary.org/api/books?bibkeys=ISBN:{isbn}&jscmd=data&format=json).
 *
 * <p>La réponse brute d'Open Library est une map dont la clé est le bibkey (ex:
 * "ISBN:9780980200447"). Bakary extrait le noeud JSON correspondant à cette clé (le "bookNode")
 * puis le convertit vers cette classe via ObjectMapper#treeToValue(bookNode,
 * OpenLibraryResponse.class). @JsonIgnoreProperties(ignoreUnknown = true) est indispensable : Open
 * Library renvoie beaucoup de champs (cover, identifiers, subjects, publishers, ...) que nous
 * n'utilisons pas. Sans cette annotation, Jackson lèverait une exception au premier champ inconnu.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryResponse {

  private String title;

  private String subtitle;

  private String url;

  @JsonProperty("publish_date")
  private String publishDate;

  private List<Author> authors;

  @Getter
  @Setter
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Author {
    private String name;
    private String url;
  }
}
