package com.example.demo.librairie.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleBooksResponse {

  private Integer totalItems;

  private List<Item> items;

  @Getter
  @Setter
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Item {
    private String id;
    private VolumeInfo volumeInfo;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class VolumeInfo {

    private String title;

    private String subtitle;

    private List<String> authors;

    private String publisher;

    @JsonProperty("publishedDate")
    private String publishedDate;

    private String description;

    private Integer pageCount;

    private String language;

    @JsonProperty("infoLink")
    private String infoLink;

    @JsonProperty("imageLinks")
    private ImageLinks imageLinks;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ImageLinks {
    private String smallThumbnail;
    private String thumbnail;
  }
}
