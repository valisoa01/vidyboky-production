package com.example.demo.librairie.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {

  @Test
  void messageIncludesResourceAndId() {
    UUID id = UUID.randomUUID();

    ResourceNotFoundException ex = new ResourceNotFoundException("Author", id);

    assertEquals("Author not found with id: " + id, ex.getMessage());
  }

  @Test
  void messageIncludesResourceFieldAndValue() {
    ResourceNotFoundException ex =
        new ResourceNotFoundException("Author", "fullName", "Victor Hugo");

    assertEquals("Author not found with fullName: Victor Hugo", ex.getMessage());
  }

  @Test
  void duplicateResourceExceptionMessageIncludesResourceFieldAndValue() {
    DuplicateResourceException ex =
        new DuplicateResourceException("Author", "fullName", "Victor Hugo");

    assertEquals("Author already exists with fullName: Victor Hugo", ex.getMessage());
  }
}
