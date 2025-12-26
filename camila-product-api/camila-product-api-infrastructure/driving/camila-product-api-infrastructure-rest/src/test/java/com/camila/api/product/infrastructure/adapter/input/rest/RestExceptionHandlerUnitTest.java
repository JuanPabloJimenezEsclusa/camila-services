package com.camila.api.product.infrastructure.adapter.input.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Set;
import java.util.stream.Stream;

import com.camila.api.product.domain.exception.NotFoundException;
import com.camila.api.product.domain.exception.ProductException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("[UT][RestExceptionHandler] REST Exception Handler Unit Tests")
class RestExceptionHandlerUnitTest {

  @InjectMocks
  private RestExceptionHandler restExceptionHandler;

  private static Stream<Arguments> notFoundExceptionParams() {
    return Stream.of(
      arguments("Product not found", "Product not found"),
      arguments("Custom not found message", "Custom not found message"));
  }

  private static Stream<Arguments> illegalArgumentExceptionParams() {
    return Stream.of(
      arguments("Invalid argument", "Invalid argument"),
      arguments(null, "Bad Request"),
      arguments("Invalid product ID format", "Invalid product ID format"));
  }

  private static Stream<Arguments> productExceptionParams() {
    return Stream.of(
      arguments(new ProductException(new RuntimeException("Database error")), "java.lang.RuntimeException: Database error"),
      arguments(new Exception("Service unavailable"), "Service unavailable"),
      arguments(new RuntimeException("Unexpected error"), "Unexpected error"),
      arguments(new RuntimeException(), "Internal Server Error"));
  }

  private static Stream<Arguments> constraintViolationParams() {
    return Stream.of(
      arguments("field1", "must not be null", "field1"),
      arguments("product.name", "size must be between 1 and 100", "name"),
      arguments("items[0].price", "must be greater than 0", "price"),
      arguments("", "must not be empty", ""));
  }

  @ParameterizedTest
  @MethodSource("notFoundExceptionParams")
  @DisplayName("Should handle NotFoundException with different messages")
  void shouldHandleNotFoundException(final String exceptionMessage, final String expectedDetail) {
    // Given
    final var exception = exceptionMessage != null ? new NotFoundException() {
      @Override
      public String getMessage() {
        return exceptionMessage;
      }
    } : new NotFoundException();
    final var request = mock(ServerHttpRequest.class);
    final var uri = URI.create("http://localhost:8080/api/products/123");
    when(request.getURI()).thenReturn(uri);

    // When
    final var response = this.restExceptionHandler.handleNotFound(exception, request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("application/problem+json"));

    final var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getType()).isEqualTo("/problems/not-found");
    assertThat(body.getTitle()).isEqualTo("NO_CONTENT");
    assertThat(body.getStatus()).isEqualTo(204);
    assertThat(body.getDetail()).isEqualTo(expectedDetail);
    assertThat(body.getInstance()).isEqualTo(uri.toString());
    assertThat(body.getErrors()).isNullOrEmpty();

    verify(request).getURI();
    verifyNoMoreInteractions(request);
  }

  @ParameterizedTest
  @MethodSource("illegalArgumentExceptionParams")
  @DisplayName("Should handle IllegalArgumentException with different messages")
  void shouldHandleIllegalArgumentException(final String exceptionMessage, final String expectedDetail) {
    // Given
    final var exception = new IllegalArgumentException(exceptionMessage);
    final var request = mock(ServerHttpRequest.class);
    final var uri = URI.create("http://localhost:8080/api/products");
    when(request.getURI()).thenReturn(uri);

    // When
    final var response = this.restExceptionHandler.handleIllegalArgument(exception, request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("application/problem+json"));

    final var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getType()).isEqualTo("/problems/invalid-argument");
    assertThat(body.getTitle()).isEqualTo("BAD_REQUEST");
    assertThat(body.getStatus()).isEqualTo(400);
    assertThat(body.getDetail()).isEqualTo(expectedDetail);
    assertThat(body.getInstance()).isEqualTo(uri.toString());
    assertThat(body.getErrors()).isNullOrEmpty();

    verify(request).getURI();
    verifyNoMoreInteractions(request);
  }

  @ParameterizedTest
  @MethodSource("productExceptionParams")
  @DisplayName("Should handle product service exceptions with different types")
  void shouldHandleProductServiceException(final Throwable exception, final String expectedDetail) {
    // Given
    final var request = mock(ServerHttpRequest.class);
    final var uri = URI.create("http://localhost:8080/api/products/sort");
    when(request.getURI()).thenReturn(uri);

    // When
    final var response = this.restExceptionHandler.handleProductServiceException(exception, request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("application/problem+json"));

    final var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getType()).isEqualTo("/problems/internal");
    assertThat(body.getTitle()).isEqualTo("INTERNAL_SERVER_ERROR");
    assertThat(body.getStatus()).isEqualTo(500);
    assertThat(body.getDetail()).isEqualTo(expectedDetail);
    assertThat(body.getInstance()).isEqualTo(uri.toString());
    assertThat(body.getErrors()).isNullOrEmpty();

    verify(request).getURI();
    verifyNoMoreInteractions(request);
  }

  @ParameterizedTest
  @MethodSource("constraintViolationParams")
  @DisplayName("Should handle ConstraintViolationException with different field paths")
  void shouldHandleConstraintViolationException(final String propertyPath, final String message,
                                                final String expectedFieldName) {
    // Given
    final var violation = this.createConstraintViolation(propertyPath, message);
    final var exception = new ConstraintViolationException(Set.of(violation));
    final var request = mock(ServerHttpRequest.class);
    final var uri = URI.create("http://localhost:8080/api/products");
    when(request.getURI()).thenReturn(uri);

    // When
    final var response = this.restExceptionHandler.handleConstraintViolation(exception, request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.EXPECTATION_FAILED);
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("application/problem+json"));

    final var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getType()).isEqualTo("/problems/validation");
    assertThat(body.getTitle()).isEqualTo("EXPECTATION_FAILED");
    assertThat(body.getStatus()).isEqualTo(417);
    assertThat(body.getInstance()).isEqualTo(uri.toString());
    assertThat(body.getErrors()).isNotNull().containsKey(expectedFieldName);
    assertThat(body.getErrors().get(expectedFieldName)).containsExactly(message);

    verify(request).getURI();
    verifyNoMoreInteractions(request);
  }

  @Test
  @DisplayName("Should handle ConstraintViolationException with multiple violations for same field")
  void shouldHandleConstraintViolationExceptionWithMultipleViolationsForSameField() {
    // Given
    final var violation1 = this.createConstraintViolation("name", "must not be null");
    final var violation2 = this.createConstraintViolation("name", "size must be between 1 and 100");
    final var exception = new ConstraintViolationException(Set.of(violation1, violation2));
    final var request = mock(ServerHttpRequest.class);
    final var uri = URI.create("http://localhost:8080/api/products");
    when(request.getURI()).thenReturn(uri);

    // When
    final var response = this.restExceptionHandler.handleConstraintViolation(exception, request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.EXPECTATION_FAILED);

    final var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getErrors()).isNotNull().containsKey("name");
    assertThat(body.getErrors().get("name")).hasSize(2)
      .containsExactlyInAnyOrder("must not be null", "size must be between 1 and 100");

    verify(request).getURI();
    verifyNoMoreInteractions(request);
  }

  @Test
  @DisplayName("Should handle ConstraintViolationException with multiple fields")
  void shouldHandleConstraintViolationExceptionWithMultipleFields() {
    // Given
    final var violation1 = this.createConstraintViolation("name", "must not be null");
    final var violation2 = this.createConstraintViolation("price", "must be greater than 0");
    final var violation3 = this.createConstraintViolation("stock", "must not be negative");
    final var exception = new ConstraintViolationException(Set.of(violation1, violation2, violation3));
    final var request = mock(ServerHttpRequest.class);
    final var uri = URI.create("http://localhost:8080/api/products");
    when(request.getURI()).thenReturn(uri);

    // When
    final var response = this.restExceptionHandler.handleConstraintViolation(exception, request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.EXPECTATION_FAILED);

    final var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getErrors()).isNotNull().hasSize(3);
    assertThat(body.getErrors()).containsKeys("name", "price", "stock");
    assertThat(body.getErrors().get("name")).containsExactly("must not be null");
    assertThat(body.getErrors().get("price")).containsExactly("must be greater than 0");
    assertThat(body.getErrors().get("stock")).containsExactly("must not be negative");

    verify(request).getURI();
    verifyNoMoreInteractions(request);
  }

  @Test
  @DisplayName("Should handle ConstraintViolationException with empty violations")
  void shouldHandleConstraintViolationExceptionWithEmptyViolations() {
    // Given
    final var exception = new ConstraintViolationException(Set.of());
    final var request = mock(ServerHttpRequest.class);
    final var uri = URI.create("http://localhost:8080/api/products");
    when(request.getURI()).thenReturn(uri);

    // When
    final var response = this.restExceptionHandler.handleConstraintViolation(exception, request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.EXPECTATION_FAILED);

    final var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getType()).isEqualTo("/problems/validation");
    assertThat(body.getTitle()).isEqualTo("EXPECTATION_FAILED");
    assertThat(body.getStatus()).isEqualTo(417);
    assertThat(body.getErrors()).isNullOrEmpty();

    verify(request).getURI();
    verifyNoMoreInteractions(request);
  }

  @Test
  @DisplayName("Should handle ConstraintViolationException with null violations")
  void shouldHandleConstraintViolationExceptionWithNullViolations() {
    // Given
    final var exception = new ConstraintViolationException(null);
    final var request = mock(ServerHttpRequest.class);
    final var uri = URI.create("http://localhost:8080/api/products");
    when(request.getURI()).thenReturn(uri);

    // When
    final var response = this.restExceptionHandler.handleConstraintViolation(exception, request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.EXPECTATION_FAILED);

    final var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getErrors()).isNullOrEmpty();

    verify(request).getURI();
    verifyNoMoreInteractions(request);
  }

  @Test
  @DisplayName("Should extract field name from nested property path")
  void shouldExtractFieldNameFromNestedPropertyPath() {
    // Given
    final var violation = this.createConstraintViolation("product.details.name", "must not be null");
    final var exception = new ConstraintViolationException(Set.of(violation));
    final var request = mock(ServerHttpRequest.class);
    final var uri = URI.create("http://localhost:8080/api/products");
    when(request.getURI()).thenReturn(uri);

    // When
    final var response = this.restExceptionHandler.handleConstraintViolation(exception, request);

    // Then
    assertThat(response).isNotNull();
    final var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getErrors()).containsKey("name");

    verify(request).getURI();
    verifyNoMoreInteractions(request);
  }

  @Test
  @DisplayName("Should extract field name from array property path")
  void shouldExtractFieldNameFromArrayPropertyPath() {
    // Given
    final var violation = this.createConstraintViolation("products[0].name", "must not be blank");
    final var exception = new ConstraintViolationException(Set.of(violation));
    final var request = mock(ServerHttpRequest.class);
    final var uri = URI.create("http://localhost:8080/api/products");
    when(request.getURI()).thenReturn(uri);

    // When
    final var response = this.restExceptionHandler.handleConstraintViolation(exception, request);

    // Then
    assertThat(response).isNotNull();
    final var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getErrors()).containsKey("name");

    verify(request).getURI();
    verifyNoMoreInteractions(request);
  }

  @Test
  @DisplayName("Should extract field name from complex array property path")
  void shouldExtractFieldNameFromComplexArrayPropertyPath() {
    // Given
    final var violation = this.createConstraintViolation("categories[2].products[5].price", "must be positive");
    final var exception = new ConstraintViolationException(Set.of(violation));
    final var request = mock(ServerHttpRequest.class);
    final var uri = URI.create("http://localhost:8080/api/products");
    when(request.getURI()).thenReturn(uri);

    // When
    final var response = this.restExceptionHandler.handleConstraintViolation(exception, request);

    // Then
    assertThat(response).isNotNull();
    final var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getErrors()).containsKey("price");

    verify(request).getURI();
    verifyNoMoreInteractions(request);
  }

  @Test
  @DisplayName("Should handle ConstraintViolationException with blank property path")
  void shouldHandleConstraintViolationExceptionWithBlankPropertyPath() {
    // Given
    final var violation = this.createConstraintViolation("", "invalid request");
    final var exception = new ConstraintViolationException(Set.of(violation));
    final var request = mock(ServerHttpRequest.class);
    final var uri = URI.create("http://localhost:8080/api/products");
    when(request.getURI()).thenReturn(uri);

    // When
    final var response = this.restExceptionHandler.handleConstraintViolation(exception, request);

    // Then
    assertThat(response).isNotNull();
    final var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getErrors()).containsKey("");

    verify(request).getURI();
    verifyNoMoreInteractions(request);
  }

  @Test
  @DisplayName("Should handle ConstraintViolationException with null property path")
  void shouldHandleConstraintViolationExceptionWithNullPropertyPath() {
    // Given
    final var violation = this.createConstraintViolation(null, "validation error");
    final var exception = new ConstraintViolationException(Set.of(violation));
    final var request = mock(ServerHttpRequest.class);
    final var uri = URI.create("http://localhost:8080/api/products");
    when(request.getURI()).thenReturn(uri);

    // When
    final var response = this.restExceptionHandler.handleConstraintViolation(exception, request);

    // Then
    assertThat(response).isNotNull();
    final var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getErrors()).containsKey("");

    verify(request).getURI();
    verifyNoMoreInteractions(request);
  }

  @Test
  @DisplayName("Should filter null constraint violations")
  void shouldFilterNullConstraintViolations() {
    // Given
    final var violation = this.createConstraintViolation("name", "must not be null");
    final Set<ConstraintViolation<?>> violations = Set.of(violation);
    final var exception = new ConstraintViolationException(violations);
    final var request = mock(ServerHttpRequest.class);
    final var uri = URI.create("http://localhost:8080/api/products");
    when(request.getURI()).thenReturn(uri);

    // When
    final var response = this.restExceptionHandler.handleConstraintViolation(exception, request);

    // Then
    assertThat(response).isNotNull();
    final var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getErrors()).containsKey("name");
    assertThat(body.getErrors().get("name")).containsExactly("must not be null");

    verify(request).getURI();
    verifyNoMoreInteractions(request);
  }

  @Test
  @DisplayName("Should create problem response with all fields correctly")
  void shouldCreateProblemResponseWithAllFieldsCorrectly() {
    // Given
    final var exception = new IllegalArgumentException("Test message");
    final var request = mock(ServerHttpRequest.class);
    final var uri = URI.create("http://localhost:8080/api/products/123");
    when(request.getURI()).thenReturn(uri);

    // When
    final var response = this.restExceptionHandler.handleIllegalArgument(exception, request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getHeaders().getContentType()).isNotNull();
    assertThat(response.getHeaders().getContentType().toString()).isEqualTo("application/problem+json");

    final var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getType()).isNotBlank();
    assertThat(body.getTitle()).isNotBlank();
    assertThat(body.getStatus()).isPositive();
    assertThat(body.getDetail()).isNotBlank();
    assertThat(body.getInstance()).isNotBlank();

    verify(request).getURI();
    verifyNoMoreInteractions(request);
  }

  @Test
  @DisplayName("Should handle exception with null message using default status phrase")
  void shouldHandleExceptionWithNullMessageUsingDefaultStatusPhrase() {
    // Given
    final var exception = new RuntimeException((String) null);
    final var request = mock(ServerHttpRequest.class);
    final var uri = URI.create("http://localhost:8080/api/products");
    when(request.getURI()).thenReturn(uri);

    // When
    final var response = this.restExceptionHandler.handleProductServiceException(exception, request);

    // Then
    assertThat(response).isNotNull();
    final var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getDetail()).isEqualTo("Internal Server Error");

    verify(request).getURI();
    verifyNoMoreInteractions(request);
  }

  @Test
  @DisplayName("Should preserve URI instance in problem response")
  void shouldPreserveUriInstanceInProblemResponse() {
    // Given
    final var exception = new NotFoundException();
    final var request = mock(ServerHttpRequest.class);
    final var uri = URI.create("https://example.com:9090/api/v1/products/abc-123?filter=active");
    when(request.getURI()).thenReturn(uri);

    // When
    final var response = this.restExceptionHandler.handleNotFound(exception, request);

    // Then
    assertThat(response).isNotNull();
    final var body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getInstance()).isEqualTo(uri.toString());

    verify(request).getURI();
    verifyNoMoreInteractions(request);
  }

  private ConstraintViolation<?> createConstraintViolation(final String propertyPath, final String message) {
    final var violation = mock(ConstraintViolation.class);
    final var path = mock(Path.class);

    when(violation.getMessage()).thenReturn(message);

    if (propertyPath != null) {
      when(violation.getPropertyPath()).thenReturn(path);
      when(path.toString()).thenReturn(propertyPath);
    }

    return violation;
  }
}
