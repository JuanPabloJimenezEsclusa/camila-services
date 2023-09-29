package com.camila.api.product.application;

import java.io.Serial;

/**
 * The type Product service exception.
 */
public class ProductServiceException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new Product service exception.
   *
   * @param cause the cause
   */
  public ProductServiceException(Throwable cause) {
    super(cause);
  }
}
