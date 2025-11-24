package com.camila.api.product.infrastructure.adapter.input.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.camila.api.product.domain.exception.NotFoundException;
import com.camila.api.product.domain.exception.ProductException;
import com.camila.api.product.infrastructure.adapter.input.rest.dto.ProblemDTO;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

/**
 * The type Rest exception handler.
 */
@Slf4j
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  private static final String APPLICATION_PROBLEM_JSON = "application/problem+json";

  /**
   * Handle not found response entity.
   *
   * @param exception the exception
   * @param request the HTTP request
   * @return the response entity
   */
  @ExceptionHandler(NotFoundException.class)
  protected ResponseEntity<ProblemDTO> handleNotFound(final NotFoundException exception,
                                                      final ServerHttpRequest request) {
    if (log.isDebugEnabled()) {
      log.debug("NotFound: {}", exception.getMessage());
    }

    return buildProblemResponse(
      "/problems/not-found",
      HttpStatus.NO_CONTENT.name(),
      HttpStatus.NO_CONTENT,
      Optional.ofNullable(exception.getMessage()).orElse(HttpStatus.NO_CONTENT.getReasonPhrase()),
      request,
      Collections.emptyMap()
    );
  }

  /**
   * Handle illegal argument response entity.
   *
   * @param exception the exception
   * @param request the HTTP request
   * @return the response entity
   */
  @ExceptionHandler(IllegalArgumentException.class)
  protected ResponseEntity<ProblemDTO> handleIllegalArgument(final IllegalArgumentException exception,
                                                             final ServerHttpRequest request) {
    if (log.isDebugEnabled()) {
      log.debug("IllegalArgument: {}", exception.getMessage());
    }

    return buildProblemResponse(
      "/problems/invalid-argument",
      HttpStatus.BAD_REQUEST.name(),
      HttpStatus.BAD_REQUEST,
      Optional.ofNullable(exception.getMessage()).orElse(HttpStatus.BAD_REQUEST.getReasonPhrase()),
      request,
      Collections.emptyMap()
    );
  }

  /**
   * Handle constraint violation response entity.
   *
   * @param exception the exception
   * @param request the HTTP request
   * @return the response entity
   */
  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<ProblemDTO> handleConstraintViolation(final ConstraintViolationException exception,
                                                                 final ServerHttpRequest request) {
    if (log.isDebugEnabled()) {
      log.debug("ConstraintViolation: {}", exception.getMessage());
    }

    final Map<String, List<String>> errors = new HashMap<>();
    if (exception.getConstraintViolations() != null) {
      exception.getConstraintViolations()
        .stream()
        .filter(Objects::nonNull)
        .forEach(cv -> {
          final String rawPath = cv.getPropertyPath() != null ? cv.getPropertyPath().toString() : "";
          final String field = extractFieldName(rawPath);
          errors.computeIfAbsent(field, _ -> new ArrayList<>()).add(cv.getMessage());
        });
    }

    return buildProblemResponse(
      "/problems/validation",
      HttpStatus.EXPECTATION_FAILED.name(),
      HttpStatus.EXPECTATION_FAILED,
      Optional.ofNullable(exception.getMessage()).orElse(HttpStatus.EXPECTATION_FAILED.getReasonPhrase()),
      request,
      errors
    );
  }

  /**
   * Handle product service exception response entity.
   *
   * @param exception the exception
   * @param request the HTTP request
   * @return the response entity
   */
  @ExceptionHandler({ProductException.class, Exception.class, RuntimeException.class})
  protected ResponseEntity<ProblemDTO> handleProductServiceException(final Throwable exception,
                                                                     final ServerHttpRequest request) {
    if (log.isErrorEnabled()) {
      log.error("ProductException: {}", exception.getMessage(), exception);
    }

    return buildProblemResponse(
      "/problems/internal",
      HttpStatus.INTERNAL_SERVER_ERROR.name(),
      HttpStatus.INTERNAL_SERVER_ERROR,
      Optional.ofNullable(exception.getMessage()).orElse(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()),
      request,
      Collections.emptyMap()
    );
  }

  private ResponseEntity<ProblemDTO> buildProblemResponse(final String type,
                                                          final String title,
                                                          final HttpStatus status,
                                                          final String detail,
                                                          final ServerHttpRequest request,
                                                          final Map<String, List<String>> errors) {
    final var problemDTO = new ProblemDTO()
      .type(type)
      .status(status.value())
      .title(title)
      .detail(detail)
      .instance(request.getURI().toString());

    if (!errors.isEmpty()) {
      problemDTO.errors(errors);
    }

    return ResponseEntity.status(status)
      .contentType(MediaType.parseMediaType(APPLICATION_PROBLEM_JSON))
      .body(problemDTO);
  }

  private String extractFieldName(final String propertyPath) {
    if (propertyPath.isBlank()) {
      return "";
    }
    final String[] byDot = propertyPath.split("\\.", -1);
    String last = byDot[byDot.length - 1];
    int bracket = last.indexOf('[');
    if (bracket > -1) {
      last = last.substring(0, bracket);
    }
    return last;
  }
}
