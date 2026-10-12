package com.camila.api.product.infrastructure.adapter.input.graphql;

import com.camila.api.product.domain.exception.NotFoundException;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class ProductGraphqlExceptionHandler {

  @GraphQlExceptionHandler
  public Mono<GraphQLError> handle(final NotFoundException ex, final DataFetchingEnvironment env) {
    return Mono.justOrEmpty(GraphQLError.newError()
      .path(env.getExecutionStepInfo().getPath())
      .message(ex.getMessage())
      .errorType(ErrorType.NOT_FOUND)
      .build());
  }
}
