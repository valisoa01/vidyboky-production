package com.example.demo.librairie.exception;

public class DuplicateResourceException extends RuntimeException {

  public DuplicateResourceException(String resource, String field, String value) {
    super(resource + " already exists with " + field + ": " + value);
  }
}
