package com.camila.api.product.infrastructure.adapter.input.rsocket;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.stream.Stream;

import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.usecase.ProductUseCase;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("[UT][ProductRSocketAdapter] Product RSocket Adapter Unit Tests")
class ProductRSocketAdapterUnitTest {

  @Mock
  private ProductUseCase productUseCase;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private JsonNode jsonNode;

  @InjectMocks
  private ProductRSocketAdapter productRSocketAdapter;

  private static Stream<Arguments> sortProductsParams() {
    return Stream.of(
      Arguments.of("0.0", "1.0", "0", "10"),
      Arguments.of("0.5", "0.5", "1", "20"),
      Arguments.of("0.8", "0.2", "2", "50"),
      Arguments.of("0.3", "0.7", "5", "15")
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

    when(objectMapper.readTree(message)).thenReturn(jsonNode);
    when(jsonNode.hasNonNull("internalId")).thenReturn(true);
    when(jsonNode.get("internalId")).thenReturn(jsonNode);
    when(jsonNode.asText(anyString())).thenReturn(internalId);
    when(productUseCase.findByInternalId(internalId)).thenReturn(Mono.just(expectedProduct));

    // When & Then
    productRSocketAdapter.findByInternalId(message)
      .as(StepVerifier::create)
      .expectNext(expectedProduct)
      .verifyComplete();

    verify(objectMapper).readTree(message);
    verify(jsonNode).hasNonNull("internalId");
    verify(jsonNode).get("internalId");
    verify(productUseCase).findByInternalId(internalId);
    verifyNoMoreInteractions(productUseCase);
  }

  @Test
  @DisplayName("Should throw exception when internalId is missing")
  void shouldThrowExceptionWhenInternalIdIsMissing() throws Exception {
    // Given
    final var message = "{}";
    when(objectMapper.readTree(message)).thenReturn(jsonNode);
    when(jsonNode.hasNonNull("internalId")).thenReturn(false);

    // When & Then
    productRSocketAdapter.findByInternalId(message)
      .as(StepVerifier::create)
      .expectError(IllegalArgumentException.class)
      .verify();

    verify(objectMapper).readTree(message);
    verify(jsonNode).hasNonNull("internalId");
    verifyNoMoreInteractions(productUseCase);
  }

  @Test
  @DisplayName("Should throw exception when product is not found")
  void shouldThrowExceptionWhenProductIsNotFound() throws Exception {
    // Given
    final var internalId = "123";
    final var message = """
        {"internalId":"%s"}
      """.formatted(internalId);

    when(objectMapper.readTree(message)).thenReturn(jsonNode);
    when(jsonNode.hasNonNull("internalId")).thenReturn(true);
    when(jsonNode.get("internalId")).thenReturn(jsonNode);
    when(jsonNode.asText(anyString())).thenReturn(internalId);
    when(productUseCase.findByInternalId(internalId)).thenReturn(Mono.empty());

    // When & Then
    productRSocketAdapter.findByInternalId(message)
      .as(StepVerifier::create)
      .expectError(IllegalArgumentException.class)
      .verify();

    verify(objectMapper).readTree(message);
    verify(jsonNode).hasNonNull("internalId");
    verify(jsonNode).get("internalId");
    verify(productUseCase).findByInternalId(internalId);
    verifyNoMoreInteractions(productUseCase);
  }

  @ParameterizedTest(name = "salesUnits={0}, stock={1}, page={2}, size={3}")
  @MethodSource("sortProductsParams")
  @DisplayName("Should sort products with different parameters")
  void shouldSortProductsByMetricsWeights(final String salesUnits, final String stock,
                                          final String page, final String size) throws Exception {
    // Given
    final var message = """
        {
          "salesUnits": "%s",
          "stock": "%s",
          "page": "%s",
          "size": "%s"
        }
      """.formatted(salesUnits, stock, page, size);

    final var product1 = Instancio.of(Product.class).create();
    final var product2 = Instancio.of(Product.class).create();
    final var expectedParams = Map.of(
      "salesUnits", salesUnits,
      "stock", stock,
      "page", page,
      "size", size
    );

    when(objectMapper.readTree(message)).thenReturn(jsonNode);
    when(jsonNode.hasNonNull(anyString())).thenReturn(true);
    when(jsonNode.get("salesUnits")).thenReturn(jsonNode);
    when(jsonNode.get("stock")).thenReturn(jsonNode);
    when(jsonNode.get("page")).thenReturn(jsonNode);
    when(jsonNode.get("size")).thenReturn(jsonNode);
    when(jsonNode.asText("0.001")).thenReturn(salesUnits);
    when(jsonNode.asText("0.999")).thenReturn(stock);
    when(jsonNode.asText("0")).thenReturn(page);
    when(jsonNode.asText("25")).thenReturn(size);
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

    when(objectMapper.readTree(message)).thenReturn(jsonNode);
    when(jsonNode.hasNonNull("salesUnits")).thenReturn(false);

    // When & Then
    productRSocketAdapter.sortByMetricsWeights(message)
      .as(StepVerifier::create)
      .expectError(IllegalArgumentException.class)
      .verify();

    verify(objectMapper).readTree(message);
    verify(jsonNode).hasNonNull("salesUnits");
    verifyNoMoreInteractions(productUseCase);
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

    when(objectMapper.readTree(message)).thenReturn(jsonNode);
    when(jsonNode.hasNonNull(anyString())).thenReturn(true);
    when(jsonNode.get(anyString())).thenReturn(jsonNode);
    when(jsonNode.asText(anyString())).thenReturn("0.5", "0.5", "0", "10");
    when(productUseCase.sortByMetricsWeights(anyMap())).thenReturn(Flux.empty());

    // When & Then
    productRSocketAdapter.sortByMetricsWeights(message)
      .as(StepVerifier::create)
      .expectError(IllegalArgumentException.class)
      .verify();

    verify(objectMapper).readTree(message);
    verify(productUseCase).sortByMetricsWeights(anyMap());
    verifyNoMoreInteractions(productUseCase);
  }
}
