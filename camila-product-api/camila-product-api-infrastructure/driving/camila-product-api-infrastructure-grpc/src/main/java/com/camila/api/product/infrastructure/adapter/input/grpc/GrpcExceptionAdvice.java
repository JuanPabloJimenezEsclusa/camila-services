package com.camila.api.product.infrastructure.adapter.input.grpc;

import com.camila.api.product.domain.exception.ProductException;
import io.grpc.Status;
import io.grpc.StatusException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.springframework.dao.DataAccessException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;

/**
 * The type Grpc exception advice.
 */
@Slf4j
@GrpcAdvice
public class GrpcExceptionAdvice {

  /**
   * Handle runtime exception status exception.
   *
   * @param ex the ex
   * @return the status exception
   */
  @SuppressWarnings("unused")
  @GrpcExceptionHandler(RuntimeException.class)
  public StatusException handleRuntimeException(final RuntimeException ex) {
    final var status = Status.INTERNAL.withDescription(ex.getLocalizedMessage()).withCause(ex);
    log.debug("(GrpcExceptionAdvice) RuntimeException", ex);
    return status.asException();
  }

  /**
   * Handle product exception status exception.
   *
   * @param ex the ex
   * @return the status exception
   */
  @SuppressWarnings("unused")
  @GrpcExceptionHandler(ProductException.class)
  public StatusException handleProductException(final ProductException ex) {
    final var status = Status.FAILED_PRECONDITION.withDescription(ex.getLocalizedMessage()).withCause(ex);
    log.debug("(GrpcExceptionAdvice) ProductException", ex);
    return status.asException();
  }

  /**
   * Handle data access exception status exception.
   *
   * @param ex the ex
   * @return the status exception
   */
  @SuppressWarnings("unused")
  @GrpcExceptionHandler(DataAccessException.class)
  public StatusException handleDataAccessException(final DataAccessException ex) {
    final var status = Status.INVALID_ARGUMENT.withDescription(ex.getLocalizedMessage()).withCause(ex);
    log.debug("(GrpcExceptionAdvice) DataAccessException", ex);
    return status.asException();
  }

  /**
   * Handle constraint violation exception status exception.
   *
   * @param ex the ex
   * @return the status exception
   */
  @SuppressWarnings("unused")
  @GrpcExceptionHandler(ConstraintViolationException.class)
  public StatusException handleConstraintViolationException(final ConstraintViolationException ex) {
    final var status = Status.INVALID_ARGUMENT.withDescription(ex.getLocalizedMessage()).withCause(ex);
    log.debug("(GrpcExceptionAdvice) ConstraintViolationException", ex);
    return status.asException();
  }

  /**
   * Handle method argument not valid exception status exception.
   *
   * @param ex the ex
   * @return the status exception
   */
  @SuppressWarnings("unused")
  @GrpcExceptionHandler(MethodArgumentNotValidException.class)
  public StatusException handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex) {
    final var status = Status.INVALID_ARGUMENT.withDescription(ex.getLocalizedMessage()).withCause(ex);
    log.debug("(GrpcExceptionAdvice) MethodArgumentNotValidException", ex);
    return status.asException();
  }

  /**
   * Handle illegal argument exception status exception.
   *
   * @param ex the ex
   * @return the status exception
   */
  @SuppressWarnings("unused")
  @GrpcExceptionHandler(IllegalArgumentException.class)
  public StatusException handleIllegalArgumentException(final IllegalArgumentException ex) {
    final var status = Status.INVALID_ARGUMENT.withDescription(ex.getLocalizedMessage()).withCause(ex);
    log.debug("(GrpcExceptionAdvice) IllegalArgumentException", ex);
    return status.asException();
  }
}
