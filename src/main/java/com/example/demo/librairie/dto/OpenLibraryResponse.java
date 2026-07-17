package com.example.demo.librairie.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

  private Object description;

  @Getter
  @Setter
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Author {
    private String name;
    private String url;
  }

  public String getDescriptionText() {
    if (description == null) {
      return null;
    }
    if (description instanceof Map) {
      Object value = ((Map<?, ?>) description).get("value");
      return value != null ? value.toString() : null;
    }
    return description.toString();
  }
}
