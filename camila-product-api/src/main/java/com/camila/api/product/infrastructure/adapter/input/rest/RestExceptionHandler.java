package com.camila.api.product.infrastructure.adapter.input.rest;

import com.camila.api.product.domain.exception.NotFoundException;
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
   * Handle illegal argument response entity.
   *
   * @param exception the exception
   * @return the response entity
   */
  @ExceptionHandler(IllegalArgumentException.class)
  protected ResponseEntity<String> handleIllegalArgument(final RuntimeException exception) {
    log.debug(exception.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
  }

  /**
   * Handle not found response entity.
   *
   * @param exception the exception
   * @return the response entity
   */
  @ExceptionHandler(NotFoundException.class)
  protected ResponseEntity<String> handleNotFound(final NotFoundException exception) {
    log.debug(exception.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }

  /**
   * Handle constraint violation response entity.
   *
   * @param exception the exception
   * @return the response entity
   */
  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<String> handleConstraintViolation(final RuntimeException exception) {
    log.debug(exception.getMessage());
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
    log.debug(exception.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }
}
