package com.camila.api.product.framework.adapter.input.rest.exception;

import com.camila.api.product.application.exception.ProductException;
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
  @ExceptionHandler(ProductException.class)
  protected ResponseEntity<String> handleProductServiceException(ProductException exception) {
    log.error(exception.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }
}
