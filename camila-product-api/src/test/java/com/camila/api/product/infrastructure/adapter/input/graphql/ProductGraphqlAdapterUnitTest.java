package com.camila.api.product.infrastructure.adapter.input.graphql;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.stream.Stream;

import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.usecase.ProductUseCase;
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
@DisplayName("[UT][ProductGraphqlAdapter] Product GraphQL Adapter Unit Tests")
class ProductGraphqlAdapterUnitTest {

  @Mock
  private ProductUseCase productUseCase;

  @InjectMocks
  private ProductGraphqlAdapter productGraphqlAdapter;

  private static Stream<Arguments> sortProductsParams() {
    // salesUnits, stock, profitMargin, daysInStock, page, size
    return Stream.of(
      arguments(0.0f, 0.0f, 0.0f, 0.0f, 0, 10),
      arguments(0.7f, 0.3f, 0.0f, 0.0f, 0, 20),
      arguments(1.0f, 1.0f, 1.0f, 1.0f, 5, 15),
      arguments(0.2f, 0.6f, 0.1f, 0.1f, 2, 25));
  }

  @Test
  @DisplayName("Should find product by internal ID")
  void shouldFindProductById() {
    // Given
    final String internalId = "123";
    final var expectedProduct = Instancio.of(Product.class).create();
    when(this.productUseCase.findByInternalId(anyString())).thenReturn(Mono.just(expectedProduct));

    // When & Then
    this.productGraphqlAdapter.findById(internalId).as(StepVerifier::create).expectNext(expectedProduct)
      .verifyComplete();

    verify(this.productUseCase).findByInternalId(internalId);
    verifyNoMoreInteractions(this.productUseCase);
  }

  @ParameterizedTest(name = "{index} -> salesUnits={0}, stock={1}, page={2}, size={3}")
  @MethodSource("sortProductsParams")
  @DisplayName("Should sort products with different parameters")
  void shouldSortProducts(final Float salesUnits, final Float stock, final Float profitMargin,
                          final Float daysInStock, final Integer page, final Integer size) {
    // Given
    final var product1 = Instancio.of(Product.class).create();
    final var product2 = Instancio.of(Product.class).create();
    final var expectedParams = Map.of("salesUnits", salesUnits.toString(), "stock", stock.toString(),
      "profitMargin", profitMargin.toString(), "daysInStock", daysInStock.toString(), "page", page.toString(),
      "size", size.toString());

    when(this.productUseCase.sortByMetricsWeights(anyMap())).thenReturn(Flux.just(product1, product2));

    // When & Then
    this.productGraphqlAdapter.sortProducts(salesUnits, stock, profitMargin, daysInStock, page, size)
      .as(StepVerifier::create).expectNext(product1).expectNext(product2).verifyComplete();

    verify(this.productUseCase).sortByMetricsWeights(expectedParams);
    verifyNoMoreInteractions(this.productUseCase);
  }

  @Test
  @DisplayName("Should handle empty result when sorting products")
  void shouldHandleEmptyResultWhenSortingProducts() {
    // Given
    final Float salesUnits = 0.25f;
    final Float stock = 0.65f;
    final Float profitMargin = 0.05f;
    final Float daysInStock = 0.05f;
    final Integer page = 0;
    final Integer size = 10;

    when(this.productUseCase.sortByMetricsWeights(anyMap())).thenReturn(Flux.empty());

    // When & Then
    this.productGraphqlAdapter.sortProducts(salesUnits, stock, profitMargin, daysInStock, page, size)
      .as(StepVerifier::create).verifyComplete();

    verify(this.productUseCase).sortByMetricsWeights(anyMap());
    verifyNoMoreInteractions(this.productUseCase);
  }
}
