package com.camila.api.product.infrastructure.adapter.input.rest;

import com.camila.api.product.domain.exception.ProductException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

/**
 * The type Rest exception handler.
 */
@Slf4j
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  /**
   * Handle constraint violation response entity.
   *
   * @param exception the exception
   * @return the response entity
   */
  @ExceptionHandler({ConstraintViolationException.class, IllegalArgumentException.class})
  protected ResponseEntity<String> handleConstraintViolation(final RuntimeException exception) {
    log.error(exception.getMessage());
    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
  }

  /**
   * Handle product service exception response entity.
   *
   * @param exception the exception
   * @return the response entity
   */
  @ExceptionHandler(ProductException.class)
  protected ResponseEntity<String> handleProductServiceException(final ProductException exception) {
    log.error(exception.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }
}
