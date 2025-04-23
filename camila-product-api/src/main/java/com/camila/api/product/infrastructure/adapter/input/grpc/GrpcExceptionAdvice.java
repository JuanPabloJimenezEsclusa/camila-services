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
  @GrpcExceptionHandler(RuntimeException.class)
  public StatusException handleRuntimeException(final RuntimeException ex) {
    var status = Status.INTERNAL.withDescription(ex.getLocalizedMessage()).withCause(ex);
    log.error("(GrpcExceptionAdvice) RuntimeException: ", ex);
    return status.asException();
  }

  /**
   * Handle product exception status exception.
   *
   * @param ex the ex
   * @return the status exception
   */
  @GrpcExceptionHandler(ProductException.class)
  public StatusException handleProductException(final ProductException ex) {
    var status = Status.FAILED_PRECONDITION.withDescription(ex.getLocalizedMessage()).withCause(ex);
    log.error("(GrpcExceptionAdvice) ProductException: ", ex);
    return status.asException();
  }

  /**
   * Handle data access exception status exception.
   *
   * @param ex the ex
   * @return the status exception
   */
  @GrpcExceptionHandler(DataAccessException.class)
  public StatusException handleDataAccessException(final DataAccessException ex) {
    var status = Status.INVALID_ARGUMENT.withDescription(ex.getLocalizedMessage()).withCause(ex);
    log.error("(GrpcExceptionAdvice) DataAccessException: ", ex);
    return status.asException();
  }

  /**
   * Handle constraint violation exception status exception.
   *
   * @param ex the ex
   * @return the status exception
   */
  @GrpcExceptionHandler(ConstraintViolationException.class)
  public StatusException handleConstraintViolationException(final ConstraintViolationException ex) {
    var status = Status.INVALID_ARGUMENT.withDescription(ex.getLocalizedMessage()).withCause(ex);
    log.error("(GrpcExceptionAdvice) ConstraintViolationException: ", ex);
    return status.asException();
  }

  /**
   * Handle method argument not valid exception status exception.
   *
   * @param ex the ex
   * @return the status exception
   */
  @GrpcExceptionHandler(MethodArgumentNotValidException.class)
  public StatusException handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex) {
    var status = Status.INVALID_ARGUMENT.withDescription(ex.getLocalizedMessage()).withCause(ex);
    log.error("(GrpcExceptionAdvice) MethodArgumentNotValidException: ", ex);
    return status.asException();
  }

  /**
   * Handle illegal argument exception status exception.
   *
   * @param ex the ex
   * @return the status exception
   */
  @GrpcExceptionHandler(IllegalArgumentException.class)
  public StatusException handleIllegalArgumentException(final IllegalArgumentException ex) {
    var status = Status.INVALID_ARGUMENT.withDescription(ex.getLocalizedMessage()).withCause(ex);
    log.error("(GrpcExceptionAdvice) IllegalArgumentException: ", ex);
    return status.asException();
  }
}
