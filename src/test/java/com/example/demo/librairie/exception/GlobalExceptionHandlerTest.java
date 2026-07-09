package com.example.demo.librairie.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;
  private HttpServletRequest request;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
    request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/api/authors/123");
  }

  @Test
  void handleNotFound_returns404WithMessageAndPath() {
    ResourceNotFoundException ex = new ResourceNotFoundException("Author", UUID.randomUUID());

    ResponseEntity<ApiError> response = handler.handleNotFound(ex, request);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
    assertEquals(ex.getMessage(), response.getBody().getMessage());
    assertEquals("/api/authors/123", response.getBody().getPath());
  }

  @Test
  void handleDuplicate_returns409WithMessageAndPath() {
    DuplicateResourceException ex =
        new DuplicateResourceException("Author", "fullName", "Victor Hugo");

    ResponseEntity<ApiError> response = handler.handleDuplicate(ex, request);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
    assertEquals(ex.getMessage(), response.getBody().getMessage());
    assertEquals("/api/authors/123", response.getBody().getPath());
  }

  @Test
  void handleTypeMismatch_returns400WithFormattedMessage() {
    MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
    when(ex.getValue()).thenReturn("not-a-uuid");
    when(ex.getName()).thenReturn("id");

    ResponseEntity<ApiError> response = handler.handleTypeMismatch(ex, request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
    assertEquals("Invalid value 'not-a-uuid' for parameter 'id'", response.getBody().getMessage());
    assertEquals("/api/authors/123", response.getBody().getPath());
  }

  @Test
  void handleGeneric_returns500WithGenericMessage() {
    Exception ex = new RuntimeException("boom");

    ResponseEntity<ApiError> response = handler.handleGeneric(ex, request);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
    assertEquals("An unexpected error occurred", response.getBody().getMessage());
    assertEquals("/api/authors/123", response.getBody().getPath());
    assertNull(response.getBody().getValidationErrors());
  }
}
