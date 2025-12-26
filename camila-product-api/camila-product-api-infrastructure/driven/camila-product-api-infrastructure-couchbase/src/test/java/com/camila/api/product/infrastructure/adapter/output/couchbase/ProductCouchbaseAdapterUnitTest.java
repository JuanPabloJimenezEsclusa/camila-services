package com.camila.api.product.infrastructure.adapter.output.couchbase;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.stream.Stream;

import com.camila.api.product.domain.model.AppliedWeights;
import com.camila.api.product.domain.model.Product;
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
@DisplayName("[UT][ProductCouchbaseAdapter] Product Couchbase Adapter Unit Tests")
class ProductCouchbaseAdapterUnitTest {

  @Mock
  private ProductCouchbaseRepository productCouchbaseRepository;

  @Mock
  private ProductCouchbaseMapper mapper;

  @InjectMocks
  private ProductCouchbaseAdapter productCouchbaseAdapter;

  private static Stream<Arguments> findByInternalIdParams() {
    return Stream.of(
      arguments("PROD-001", createEntity("PROD-001", "Product 1"), createProduct("PROD-001", "Product 1")),
      arguments("PROD-123", createEntity("PROD-123", "Product 123"), createProduct("PROD-123", "Product 123")),
      arguments("TEST-999", createEntity("TEST-999", "Test Product"), createProduct("TEST-999", "Test Product")),
      arguments("ABC-XYZ", createEntity("ABC-XYZ", "Special Product"), createProduct("ABC-XYZ", "Special Product")));
  }

  private static Stream<Arguments> sortByMetricsWeightsParams() {
    return Stream.of(
      arguments(new AppliedWeights(0.5, 0.3, 0.1, 0.1), 0L, 10L,
        new ProductCouchbaseEntity[]{createEntity("P1", "Product 1"), createEntity("P2", "Product 2")},
        new Product[]{createProduct("P1", "Product 1"), createProduct("P2", "Product 2")}),
      arguments(new AppliedWeights(0.25, 0.25, 0.25, 0.25), 10L, 20L,
        new ProductCouchbaseEntity[]{createEntity("P3", "Product 3")},
        new Product[]{createProduct("P3", "Product 3")}),
      arguments(new AppliedWeights(1.0, 0.0, 0.0, 0.0), 0L, 5L,
        new ProductCouchbaseEntity[]{createEntity("P4", "Product 4"), createEntity("P5", "Product 5"), createEntity("P6", "Product 6")},
        new Product[]{createProduct("P4", "Product 4"), createProduct("P5", "Product 5"), createProduct("P6", "Product 6")}),
      arguments(new AppliedWeights(0.0, 1.0, 0.0, 0.0), 5L, 15L,
        new ProductCouchbaseEntity[]{createEntity("P7", "Product 7")},
        new Product[]{createProduct("P7", "Product 7")}));
  }

  private static Stream<Arguments> appliedWeightsParams() {
    return Stream.of(
      arguments(new AppliedWeights(0.5, 0.3, 0.1, 0.1), 0L, 10L, 0.5, 0.3, 0.1, 0.1, 10L, 0L),
      arguments(new AppliedWeights(0.25, 0.25, 0.25, 0.25), 10L, 20L, 0.25, 0.25, 0.25, 0.25, 20L, 10L),
      arguments(new AppliedWeights(1.0, 0.0, 0.0, 0.0), 5L, 15L, 1.0, 0.0, 0.0, 0.0, 15L, 5L),
      arguments(new AppliedWeights(0.0, 0.0, 0.0, 1.0), 100L, 200L, 0.0, 0.0, 0.0, 1.0, 200L, 100L));
  }

  private static ProductCouchbaseEntity createEntity(final String internalId, final String name) {
    return new ProductCouchbaseEntity("id-" + internalId, internalId, name, "Category", 100,
      Map.of("warehouse1", 50, "warehouse2", 30), 0.15, 30);
  }

  private static Product createProduct(final String internalId, final String name) {
    return new Product("id-" + internalId, internalId, name, "Category", 100,
      Map.of("warehouse1", 50, "warehouse2", 30), 0.15, 30);
  }

  @ParameterizedTest
  @MethodSource("findByInternalIdParams")
  @DisplayName("Should find product by internal ID and map to domain model")
  void shouldFindProductByInternalIdAndMapToDomainModel(final String internalId,
                                                        final ProductCouchbaseEntity entity,
                                                        final Product expectedProduct) {
    // Given
    when(this.productCouchbaseRepository.findByInternalId(internalId)).thenReturn(Mono.just(entity));
    when(this.mapper.toProduct(entity)).thenReturn(expectedProduct);

    // When & Then
    this.productCouchbaseAdapter.findByInternalId(internalId).as(StepVerifier::create)
      .expectNext(expectedProduct)
      .verifyComplete();

    verify(this.productCouchbaseRepository).findByInternalId(internalId);
    verify(this.mapper).toProduct(entity);
    verifyNoMoreInteractions(this.productCouchbaseRepository, this.mapper);
  }

  @Test
  @DisplayName("Should return empty when product not found by internal ID")
  void shouldReturnEmptyWhenProductNotFoundByInternalId() {
    // Given
    final var internalId = "NON-EXISTENT";
    when(this.productCouchbaseRepository.findByInternalId(internalId)).thenReturn(Mono.empty());

    // When & Then
    this.productCouchbaseAdapter.findByInternalId(internalId).as(StepVerifier::create)
      .verifyComplete();

    verify(this.productCouchbaseRepository).findByInternalId(internalId);
    verifyNoMoreInteractions(this.productCouchbaseRepository);
    verifyNoInteractions(this.mapper);
  }

  @Test
  @DisplayName("Should propagate error when repository fails on findByInternalId")
  void shouldPropagateErrorWhenRepositoryFailsOnFindByInternalId() {
    // Given
    final var internalId = "ERROR-PRODUCT";
    final var expectedException = new RuntimeException("Database connection error");
    when(this.productCouchbaseRepository.findByInternalId(internalId))
      .thenReturn(Mono.error(expectedException));

    // When & Then
    this.productCouchbaseAdapter.findByInternalId(internalId).as(StepVerifier::create)
      .expectError(RuntimeException.class)
      .verify();

    verify(this.productCouchbaseRepository).findByInternalId(internalId);
    verifyNoMoreInteractions(this.productCouchbaseRepository);
    verifyNoInteractions(this.mapper);
  }

  @Test
  @DisplayName("Should propagate error when mapper fails on findByInternalId")
  void shouldPropagateErrorWhenMapperFailsOnFindByInternalId() {
    // Given
    final var internalId = "MAPPER-ERROR";
    final var entity = createEntity(internalId, "Mapper Error Product");
    final var expectedException = new RuntimeException("Mapping error");
    when(this.productCouchbaseRepository.findByInternalId(internalId)).thenReturn(Mono.just(entity));
    when(this.mapper.toProduct(entity)).thenThrow(expectedException);

    // When & Then
    this.productCouchbaseAdapter.findByInternalId(internalId).as(StepVerifier::create)
      .expectError(RuntimeException.class)
      .verify();

    verify(this.productCouchbaseRepository).findByInternalId(internalId);
    verify(this.mapper).toProduct(entity);
    verifyNoMoreInteractions(this.productCouchbaseRepository, this.mapper);
  }

  @ParameterizedTest
  @MethodSource("sortByMetricsWeightsParams")
  @DisplayName("Should sort products by metrics weights and map to domain models")
  void shouldSortProductsByMetricsWeightsAndMapToDomainModels(final AppliedWeights weights, final long offset,
                                                              final long limit,
                                                              final ProductCouchbaseEntity[] entities,
                                                              final Product[] expectedProducts) {
    // Given
    when(this.productCouchbaseRepository.sortByMetricsWeights(
      weights.salesUnitsWeight(), weights.stockWeight(), weights.profitMarginWeight(),
      weights.daysInStockWeight(), limit, offset))
      .thenReturn(Flux.fromArray(entities));

    for (int i = 0; i < entities.length; i++) {
      when(this.mapper.toProduct(entities[i])).thenReturn(expectedProducts[i]);
    }

    // When & Then
    this.productCouchbaseAdapter.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectNext(expectedProducts)
      .verifyComplete();

    verify(this.productCouchbaseRepository).sortByMetricsWeights(
      weights.salesUnitsWeight(), weights.stockWeight(), weights.profitMarginWeight(),
      weights.daysInStockWeight(), limit, offset);

    for (final var entity : entities) {
      verify(this.mapper).toProduct(entity);
    }

    verifyNoMoreInteractions(this.productCouchbaseRepository, this.mapper);
  }

  @Test
  @DisplayName("Should return empty flux when no products match sorting criteria")
  void shouldReturnEmptyFluxWhenNoProductsMatchSortingCriteria() {
    // Given
    final var weights = new AppliedWeights(0.5, 0.3, 0.1, 0.1);
    final long offset = 0L;
    final long limit = 10L;
    when(this.productCouchbaseRepository.sortByMetricsWeights(
      weights.salesUnitsWeight(), weights.stockWeight(), weights.profitMarginWeight(),
      weights.daysInStockWeight(), limit, offset))
      .thenReturn(Flux.empty());

    // When & Then
    this.productCouchbaseAdapter.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .verifyComplete();

    verify(this.productCouchbaseRepository).sortByMetricsWeights(
      weights.salesUnitsWeight(), weights.stockWeight(), weights.profitMarginWeight(),
      weights.daysInStockWeight(), limit, offset);
    verifyNoMoreInteractions(this.productCouchbaseRepository);
    verifyNoInteractions(this.mapper);
  }

  @Test
  @DisplayName("Should propagate error when repository fails on sortByMetricsWeights")
  void shouldPropagateErrorWhenRepositoryFailsOnSortByMetricsWeights() {
    // Given
    final var weights = new AppliedWeights(0.5, 0.3, 0.1, 0.1);
    final long offset = 0L;
    final long limit = 10L;
    final var expectedException = new RuntimeException("Database sorting error");
    when(this.productCouchbaseRepository.sortByMetricsWeights(
      weights.salesUnitsWeight(), weights.stockWeight(), weights.profitMarginWeight(),
      weights.daysInStockWeight(), limit, offset))
      .thenReturn(Flux.error(expectedException));

    // When & Then
    this.productCouchbaseAdapter.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectError(RuntimeException.class)
      .verify();

    verify(this.productCouchbaseRepository).sortByMetricsWeights(
      weights.salesUnitsWeight(), weights.stockWeight(), weights.profitMarginWeight(),
      weights.daysInStockWeight(), limit, offset);
    verifyNoMoreInteractions(this.productCouchbaseRepository);
    verifyNoInteractions(this.mapper);
  }

  @Test
  @DisplayName("Should propagate error when mapper fails during sorting")
  void shouldPropagateErrorWhenMapperFailsDuringSorting() {
    // Given
    final var weights = new AppliedWeights(0.5, 0.3, 0.1, 0.1);
    final long offset = 0L;
    final long limit = 10L;
    final var entity = createEntity("ERROR", "Error Product");
    final var expectedException = new RuntimeException("Mapping error during sorting");
    when(this.productCouchbaseRepository.sortByMetricsWeights(
      weights.salesUnitsWeight(), weights.stockWeight(), weights.profitMarginWeight(),
      weights.daysInStockWeight(), limit, offset))
      .thenReturn(Flux.just(entity));
    when(this.mapper.toProduct(entity)).thenThrow(expectedException);

    // When & Then
    this.productCouchbaseAdapter.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectError(RuntimeException.class)
      .verify();

    verify(this.productCouchbaseRepository).sortByMetricsWeights(
      weights.salesUnitsWeight(), weights.stockWeight(), weights.profitMarginWeight(),
      weights.daysInStockWeight(), limit, offset);
    verify(this.mapper).toProduct(entity);
    verifyNoMoreInteractions(this.productCouchbaseRepository, this.mapper);
  }

  @ParameterizedTest
  @MethodSource("appliedWeightsParams")
  @DisplayName("Should pass correct weight parameters to repository")
  void shouldPassCorrectWeightParametersToRepository(final AppliedWeights weights, final long offset,
                                                     final long limit, final double expectedSalesWeight,
                                                     final double expectedStockWeight,
                                                     final double expectedProfitMarginWeight,
                                                     final double expectedDaysInStockWeight,
                                                     final long expectedLimit, final long expectedOffset) {
    // Given
    when(this.productCouchbaseRepository.sortByMetricsWeights(
      expectedSalesWeight, expectedStockWeight, expectedProfitMarginWeight,
      expectedDaysInStockWeight, expectedLimit, expectedOffset))
      .thenReturn(Flux.empty());

    // When & Then
    this.productCouchbaseAdapter.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .verifyComplete();

    verify(this.productCouchbaseRepository).sortByMetricsWeights(
      expectedSalesWeight, expectedStockWeight, expectedProfitMarginWeight,
      expectedDaysInStockWeight, expectedLimit, expectedOffset);
    verifyNoMoreInteractions(this.productCouchbaseRepository);
    verifyNoInteractions(this.mapper);
  }

  @Test
  @DisplayName("Should handle single product in sorting results")
  void shouldHandleSingleProductInSortingResults() {
    // Given
    final var weights = new AppliedWeights(0.5, 0.3, 0.1, 0.1);
    final long offset = 0L;
    final long limit = 1L;
    final var entity = createEntity("SINGLE", "Single Product");
    final var product = createProduct("SINGLE", "Single Product");
    when(this.productCouchbaseRepository.sortByMetricsWeights(
      weights.salesUnitsWeight(), weights.stockWeight(), weights.profitMarginWeight(),
      weights.daysInStockWeight(), limit, offset))
      .thenReturn(Flux.just(entity));
    when(this.mapper.toProduct(entity)).thenReturn(product);

    // When & Then
    this.productCouchbaseAdapter.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectNext(product)
      .verifyComplete();

    verify(this.productCouchbaseRepository).sortByMetricsWeights(
      weights.salesUnitsWeight(), weights.stockWeight(), weights.profitMarginWeight(),
      weights.daysInStockWeight(), limit, offset);
    verify(this.mapper).toProduct(entity);
    verifyNoMoreInteractions(this.productCouchbaseRepository, this.mapper);
  }

  @Test
  @DisplayName("Should handle large number of products in sorting results")
  void shouldHandleLargeNumberOfProductsInSortingResults() {
    // Given
    final var weights = new AppliedWeights(0.5, 0.3, 0.1, 0.1);
    final long offset = 0L;
    final long limit = 100L;
    final var entities = Stream.iterate(1, n -> n + 1)
      .limit(100)
      .map(n -> createEntity("P" + n, "Product " + n))
      .toArray(ProductCouchbaseEntity[]::new);
    final var products = Stream.iterate(1, n -> n + 1)
      .limit(100)
      .map(n -> createProduct("P" + n, "Product " + n))
      .toArray(Product[]::new);

    when(this.productCouchbaseRepository.sortByMetricsWeights(
      weights.salesUnitsWeight(), weights.stockWeight(), weights.profitMarginWeight(),
      weights.daysInStockWeight(), limit, offset))
      .thenReturn(Flux.fromArray(entities));

    for (int i = 0; i < entities.length; i++) {
      when(this.mapper.toProduct(entities[i])).thenReturn(products[i]);
    }

    // When & Then
    this.productCouchbaseAdapter.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectNext(products)
      .verifyComplete();

    verify(this.productCouchbaseRepository).sortByMetricsWeights(
      weights.salesUnitsWeight(), weights.stockWeight(), weights.profitMarginWeight(),
      weights.daysInStockWeight(), limit, offset);

    for (final var entity : entities) {
      verify(this.mapper).toProduct(entity);
    }

    verifyNoMoreInteractions(this.productCouchbaseRepository, this.mapper);
  }

  @Test
  @DisplayName("Should handle zero weights")
  void shouldHandleZeroWeights() {
    // Given
    final var weights = new AppliedWeights(0.0, 0.0, 0.0, 0.0);
    final long offset = 0L;
    final long limit = 10L;
    when(this.productCouchbaseRepository.sortByMetricsWeights(0.0, 0.0, 0.0, 0.0, limit, offset))
      .thenReturn(Flux.empty());

    // When & Then
    this.productCouchbaseAdapter.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .verifyComplete();

    verify(this.productCouchbaseRepository).sortByMetricsWeights(0.0, 0.0, 0.0, 0.0, limit, offset);
    verifyNoMoreInteractions(this.productCouchbaseRepository);
    verifyNoInteractions(this.mapper);
  }

  @Test
  @DisplayName("Should handle maximum weight values")
  void shouldHandleMaximumWeightValues() {
    // Given
    final var weights = new AppliedWeights(1.0, 1.0, 1.0, 1.0);
    final long offset = 0L;
    final long limit = 10L;
    when(this.productCouchbaseRepository.sortByMetricsWeights(1.0, 1.0, 1.0, 1.0, limit, offset))
      .thenReturn(Flux.empty());

    // When & Then
    this.productCouchbaseAdapter.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .verifyComplete();

    verify(this.productCouchbaseRepository).sortByMetricsWeights(1.0, 1.0, 1.0, 1.0, limit, offset);
    verifyNoMoreInteractions(this.productCouchbaseRepository);
    verifyNoInteractions(this.mapper);
  }

  @Test
  @DisplayName("Should handle zero offset and limit")
  void shouldHandleZeroOffsetAndLimit() {
    // Given
    final var weights = new AppliedWeights(0.5, 0.3, 0.1, 0.1);
    final long offset = 0L;
    final long limit = 0L;
    when(this.productCouchbaseRepository.sortByMetricsWeights(
      weights.salesUnitsWeight(), weights.stockWeight(), weights.profitMarginWeight(),
      weights.daysInStockWeight(), limit, offset))
      .thenReturn(Flux.empty());

    // When & Then
    this.productCouchbaseAdapter.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .verifyComplete();

    verify(this.productCouchbaseRepository).sortByMetricsWeights(
      weights.salesUnitsWeight(), weights.stockWeight(), weights.profitMarginWeight(),
      weights.daysInStockWeight(), limit, offset);
    verifyNoMoreInteractions(this.productCouchbaseRepository);
    verifyNoInteractions(this.mapper);
  }

  @Test
  @DisplayName("Should handle large offset and limit values")
  void shouldHandleLargeOffsetAndLimitValues() {
    // Given
    final var weights = new AppliedWeights(0.5, 0.3, 0.1, 0.1);
    final long offset = Long.MAX_VALUE;
    final long limit = Long.MAX_VALUE;
    when(this.productCouchbaseRepository.sortByMetricsWeights(
      weights.salesUnitsWeight(), weights.stockWeight(), weights.profitMarginWeight(),
      weights.daysInStockWeight(), limit, offset))
      .thenReturn(Flux.empty());

    // When & Then
    this.productCouchbaseAdapter.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .verifyComplete();

    verify(this.productCouchbaseRepository).sortByMetricsWeights(
      weights.salesUnitsWeight(), weights.stockWeight(), weights.profitMarginWeight(),
      weights.daysInStockWeight(), limit, offset);
    verifyNoMoreInteractions(this.productCouchbaseRepository);
    verifyNoInteractions(this.mapper);
  }
}
