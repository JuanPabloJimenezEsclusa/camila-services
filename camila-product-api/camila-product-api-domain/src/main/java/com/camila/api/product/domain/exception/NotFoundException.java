package com.camila.api.product.domain.exception;

import java.io.Serial;

/**
 * The type Not found exception.
 */
public class NotFoundException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new Not found exception.
   */
  public NotFoundException() {
    super("Product not found");
  }
}
