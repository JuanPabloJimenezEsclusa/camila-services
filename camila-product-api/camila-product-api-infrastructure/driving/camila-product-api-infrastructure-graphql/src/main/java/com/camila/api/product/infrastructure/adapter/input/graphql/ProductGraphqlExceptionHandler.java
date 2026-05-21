package com.camila.api.product.infrastructure.adapter.input.graphql;

import com.camila.api.product.domain.exception.NotFoundException;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import reactor.core.publisher.Mono;

/**
 * The type Product graphql exception handler.
 */
@ControllerAdvice
public class ProductGraphqlExceptionHandler {

  /**
   * Handle.
   *
   * @param ex  the exception
   * @param env the environment
   * @return graphql error
   */
  @GraphQlExceptionHandler
  public Mono<GraphQLError> handle(final NotFoundException ex, final DataFetchingEnvironment env) {
    return Mono.justOrEmpty(GraphQLError.newError()
      .path(env.getExecutionStepInfo().getPath())
      .message(ex.getMessage())
      .errorType(ErrorType.NOT_FOUND)
      .build());
  }
}
