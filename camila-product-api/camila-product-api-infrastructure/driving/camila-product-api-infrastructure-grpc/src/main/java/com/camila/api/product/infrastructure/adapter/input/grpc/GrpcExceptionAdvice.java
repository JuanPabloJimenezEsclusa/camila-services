package com.camila.api.product.infrastructure.adapter.input.grpc;

import com.camila.api.product.domain.exception.NotFoundException;
import com.camila.api.product.domain.exception.ProductException;
import io.grpc.Status;
import io.grpc.StatusException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;
import org.springframework.stereotype.Component;

/**
 * The type Grpc exception advice.
 */
@Slf4j
@Component
public class GrpcExceptionAdvice implements GrpcExceptionHandler {

  @Override
  public StatusException handleException(final Throwable ex) {
    log.debug("(GrpcExceptionAdvice) Throwable", ex);
    switch (ex) {
      case ProductException productEx -> {
        return Status.FAILED_PRECONDITION.withDescription(productEx.getLocalizedMessage()).withCause(productEx).asException();
      }
      case NotFoundException notFoundEx -> {
        return Status.NOT_FOUND.withDescription(notFoundEx.getLocalizedMessage()).withCause(notFoundEx).asException();
      }
      case ConstraintViolationException constraintEx -> {
        return Status.INVALID_ARGUMENT.withDescription(constraintEx.getLocalizedMessage()).withCause(constraintEx).asException();
      }
      case IllegalArgumentException illegalEx -> {
        return Status.INVALID_ARGUMENT.withDescription(illegalEx.getLocalizedMessage()).withCause(illegalEx).asException();
      }
      default -> {
        return Status.INTERNAL.withDescription(ex.getLocalizedMessage()).withCause(ex).asException();
      }
    }
  }
}
