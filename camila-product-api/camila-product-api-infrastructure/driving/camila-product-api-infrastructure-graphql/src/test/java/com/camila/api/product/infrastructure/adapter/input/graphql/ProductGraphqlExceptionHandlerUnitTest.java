package com.camila.api.product.infrastructure.adapter.input.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.camila.api.product.domain.exception.NotFoundException;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.graphql.execution.ErrorType;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("[UT][ProductGraphqlExceptionHandler] Product GraphQL Exception Handler Unit Tests")
class ProductGraphqlExceptionHandlerUnitTest {

  @InjectMocks
  private ProductGraphqlExceptionHandler exceptionHandler;

  @Mock
  private DataFetchingEnvironment env;

  @Mock
  private ExecutionStepInfo executionStepInfo;

  @Test
  @DisplayName("Should handle NotFoundException")
  void shouldHandleNotFoundException() {
    // Given
    final var exception = new NotFoundException();
    final var path = ResultPath.parse("/product");

    when(this.env.getExecutionStepInfo()).thenReturn(this.executionStepInfo);
    when(this.executionStepInfo.getPath()).thenReturn(path);

    // When & Then
    this.exceptionHandler.handle(exception, this.env)
      .as(StepVerifier::create)
      .assertNext(error -> {
        assertThat(error.getMessage()).isEqualTo("Product not found");
        assertThat(error.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        assertThat(error.getPath()).isEqualTo(path.toList());
      })
      .verifyComplete();

    verify(this.env).getExecutionStepInfo();
    verify(this.executionStepInfo).getPath();
    verifyNoMoreInteractions(this.env, this.executionStepInfo);
  }
}
