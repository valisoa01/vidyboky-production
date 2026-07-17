package com.example.demo.librairie.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // ── 404 — ressource introuvable ──────────────────────────────────────────
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(
      ResourceNotFoundException ex, HttpServletRequest request) {

    log.warn("Resource not found on {} : {}", request.getRequestURI(), ex.getMessage());
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
  }

  // ── 404 — ressource introuvable (JPA / legacy) ────────────────────────────
  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ApiError> handleEntityNotFound(
      EntityNotFoundException ex, HttpServletRequest request) {

    log.warn("Entity not found on {} : {}", request.getRequestURI(), ex.getMessage());
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
  }

  // ── 409 — doublon ────────────────────────────────────────────────────────
  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ApiError> handleDuplicate(
      DuplicateResourceException ex, HttpServletRequest request) {

    log.warn("Duplicate resource on {} : {}", request.getRequestURI(), ex.getMessage());
    return build(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), null);
  }

  // ── 400 — argument métier invalide (stock insuffisant, règle violée…) ────
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {

    log.warn("Invalid argument on {} : {}", request.getRequestURI(), ex.getMessage());
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
  }

  // ── 502 — échec technique d'un appel à un service externe ─────────────────
  @ExceptionHandler(ExternalServiceException.class)
  public ResponseEntity<ApiError> handleExternalService(
      ExternalServiceException ex, HttpServletRequest request) {

    log.error("External service call failed on {}", request.getRequestURI(), ex);
    return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request.getRequestURI(), null);
  }

  // ── 400 — erreurs de validation (@Valid) ─────────────────────────────────
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    Map<String, String> errors = new LinkedHashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      errors.put(fe.getField(), fe.getDefaultMessage());
    }

    ApiError apiError =
        ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message("Validation failed")
            .path(request.getRequestURI())
            .validationErrors(errors)
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
  }

  // ── 400 — mauvais type de paramètre (ex: UUID malformé dans l'URL) ───────
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiError> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

    String message = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'";
    return build(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), null);
  }

  // ── 500 — erreur inattendue ───────────────────────────────────────────────
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {

    // On log toujours la stacktrace complète : sans ça une erreur 500 est
    // invisible côté serveur et impossible à diagnostiquer.
    log.error("Unexpected error on {}", request.getRequestURI(), ex);

    return build(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred",
        request.getRequestURI(),
        null);
  }

  // ── Helper ────────────────────────────────────────────────────────────────
  private ResponseEntity<ApiError> build(
      HttpStatus status, String message, String path, Map<String, String> validationErrors) {

    ApiError apiError =
        ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(path)
            .validationErrors(validationErrors)
            .build();

    return ResponseEntity.status(status).body(apiError);
  }
}
