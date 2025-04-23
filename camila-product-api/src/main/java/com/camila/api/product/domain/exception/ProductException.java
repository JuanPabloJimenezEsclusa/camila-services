package com.camila.api.product.domain.exception;

import java.io.Serial;

/**
 * The type Product exception.
 */
public class ProductException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new Product exception.
   *
   * @param cause the cause
   */
  public ProductException(Throwable cause) {
    super(cause);
  }
}
