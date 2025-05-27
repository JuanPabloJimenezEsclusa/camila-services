package com.camila.api.product.domain.exception;

import java.io.Serial;

/**
 * The type ProductException.
 */
public class ProductException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new ProductException with a specified cause.
   *
   * @param cause the cause of the exception, typically another throwable
   */
  public ProductException(Throwable cause) {
    super(cause);
  }
}
