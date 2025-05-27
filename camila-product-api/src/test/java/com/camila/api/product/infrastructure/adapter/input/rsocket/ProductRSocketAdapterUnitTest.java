package com.camila.api.product.infrastructure.adapter.input.rsocket;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.stream.Stream;

import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.usecase.ProductUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("[UT][ProductRSocketAdapter] Product RSocket Adapter Unit Tests")
class ProductRSocketAdapterUnitTest {

  @Mock
  private ProductUseCase productUseCase;

  @Spy
  private ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks
  private ProductRSocketAdapter productRSocketAdapter;

  private static Stream<Arguments> sortProductsParams() {
    // salesUnits, stock, profitMargin, daysInStock, page, size
    return Stream.of(
      Arguments.of("0.0", "1.0", "0.0", "0.0", "0", "10"),
      Arguments.of("0.5", "0.5", "0.5", "0.5", "1", "20"),
      Arguments.of("0.5", "0.3", "0.1", "0.1", "2", "50"),
      Arguments.of("0.0", "0.5", "0.3", "0.2", "5", "15")
    );
  }

  @Test
  @DisplayName("Should find product by internal ID")
  void shouldFindProductByInternalId() throws Exception {
    // Given
    final var internalId = "123";
    final var message = """
        {"internalId":"%s"}
      """.formatted(internalId);
    final var expectedProduct = Instancio.of(Product.class).create();

    when(productUseCase.findByInternalId(internalId)).thenReturn(Mono.just(expectedProduct));

    // When & Then
    productRSocketAdapter.findByInternalId(message)
      .as(StepVerifier::create)
      .expectNext(expectedProduct)
      .verifyComplete();

    verify(objectMapper).readTree(message);
    verify(productUseCase).findByInternalId(internalId);
    verifyNoMoreInteractions(productUseCase);
  }

  @Test
  @DisplayName("Should throw exception when internalId is missing")
  void shouldThrowExceptionWhenInternalIdIsMissing() throws Exception {
    // Given
    final var message = "{}";

    // When & Then
    productRSocketAdapter.findByInternalId(message)
      .as(StepVerifier::create)
      .expectError(IllegalArgumentException.class)
      .verify();

    verify(objectMapper).readTree(message);
    verifyNoInteractions(productUseCase);
  }

  @Test
  @DisplayName("Should throw exception when product is not found")
  void shouldThrowExceptionWhenProductIsNotFound() throws Exception {
    // Given
    final var internalId = "123";
    final var message = """
        {"internalId":"%s"}
      """.formatted(internalId);

    when(productUseCase.findByInternalId(internalId)).thenReturn(Mono.empty());

    // When & Then
    productRSocketAdapter.findByInternalId(message)
      .as(StepVerifier::create)
      .expectError(IllegalArgumentException.class)
      .verify();

    verify(objectMapper).readTree(message);
    verify(productUseCase).findByInternalId(internalId);
    verifyNoMoreInteractions(productUseCase);
  }

  @ParameterizedTest(name = "{index} -> salesUnits={0}, stock={1}, page={2}, size={3}")
  @MethodSource("sortProductsParams")
  @DisplayName("Should sort products with different parameters")
  void shouldSortProductsByMetricsWeights(final String salesUnits, final String stock,
                                          final String profitMargin, final String daysInStock,
                                          final String page, final String size) throws Exception {
    // Given
    final var message = """
        {
          "salesUnits": "%s",
          "stock": "%s",
          "profitMargin": "%s",
          "daysInStock": "%s",
          "page": "%s",
          "size": "%s"
        }
      """.formatted(salesUnits, stock, profitMargin, daysInStock, page, size);

    final var product1 = Instancio.of(Product.class).create();
    final var product2 = Instancio.of(Product.class).create();
    final var expectedParams = Map.of(
      "salesUnits", salesUnits,
      "stock", stock,
      "profitMargin", profitMargin,
      "daysInStock", daysInStock,
      "page", page,
      "size", size
    );

    when(productUseCase.sortByMetricsWeights(expectedParams)).thenReturn(Flux.just(product1, product2));

    // When & Then
    productRSocketAdapter.sortByMetricsWeights(message)
      .as(StepVerifier::create)
      .expectNext(product1)
      .expectNext(product2)
      .verifyComplete();

    verify(objectMapper).readTree(message);
    verify(productUseCase).sortByMetricsWeights(expectedParams);
    verifyNoMoreInteractions(productUseCase);
  }

  @Test
  @DisplayName("Should throw exception when salesUnits is missing")
  void shouldThrowExceptionWhenSalesUnitsIsMissing() throws Exception {
    // Given
    final var message = """
        {
          "stock": "0.5",
          "page": "0",
          "size": "10"
        }
      """;

    // When & Then
    productRSocketAdapter.sortByMetricsWeights(message)
      .as(StepVerifier::create)
      .expectError(IllegalArgumentException.class)
      .verify();

    verify(objectMapper).readTree(message);
    verifyNoInteractions(productUseCase);
  }

  @Test
  @DisplayName("Should throw exception when no products match sorting criteria")
  void shouldThrowExceptionWhenNoProductsMatchSortingCriteria() throws Exception {
    // Given
    final var message = """
        {
          "salesUnits": "0.5",
          "stock": "0.5",
          "page": "0",
          "size": "10"
        }
      """;

    // When & Then
    productRSocketAdapter.sortByMetricsWeights(message)
      .as(StepVerifier::create)
      .expectError(IllegalArgumentException.class)
      .verify();

    verify(objectMapper).readTree(message);
    verifyNoInteractions(productUseCase);
  }
}
