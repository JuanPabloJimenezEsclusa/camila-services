package com.camila.api.product.domain.exception;

import java.io.Serial;

public class ProductException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 1L;

  public ProductException(final Throwable cause) {
    super(cause);
  }
}
