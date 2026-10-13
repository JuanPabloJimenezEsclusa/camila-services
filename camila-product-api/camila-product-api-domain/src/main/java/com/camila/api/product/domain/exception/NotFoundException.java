package com.camila.api.product.domain.exception;

import java.io.Serial;

public class NotFoundException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 1L;

  public NotFoundException() {
    super("Product not found");
  }
}
