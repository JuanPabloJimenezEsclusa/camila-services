package com.camila.api.product.presentation;

import com.camila.api.product.application.ProductServiceException;
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
class RestExceptionHandler extends ResponseEntityExceptionHandler {

  /**
   * Handle constraint violation response entity.
   *
   * @param exception the exception
   * @return the response entity
   */
  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<String> handleConstraintViolation(ConstraintViolationException exception) {
    log.error(exception.getMessage());
    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
  }

  /**
   * Handle product service exception response entity.
   *
   * @param exception the exception
   * @return the response entity
   */
  @ExceptionHandler(ProductServiceException.class)
  protected ResponseEntity<String> handleProductServiceException(ProductServiceException exception) {
    log.error(exception.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }
}
