package com.camila.api.product.infrastructure.adapter.output.cache;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.camila.api.product.domain.model.AppliedWeights;
import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.port.CachePort;
import com.camila.api.product.domain.port.ProductRepository;
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
@DisplayName("[UT][CachedProductRepositoryDecorator] Cached Product Repository Decorator Unit Tests")
class CachedProductRepositoryDecoratorUnitTest {

  @Mock
  private ProductRepository delegate;

  @Mock
  private CachePort cachePort;

  @InjectMocks
  private CachedProductRepositoryDecorator cachedProductRepositoryDecorator;

  private static Stream<Arguments> findByInternalIdCacheHitParams() {
    return Stream.of(
      arguments("PROD-001", createProduct("PROD-001", "Product 1")),
      arguments("PROD-123", createProduct("PROD-123", "Product 123")),
      arguments("TEST-999", createProduct("TEST-999", "Test Product")),
      arguments("ABC-XYZ", createProduct("ABC-XYZ", "Special Product")));
  }

  private static Stream<Arguments> findByInternalIdCacheMissParams() {
    return Stream.of(
      arguments("PROD-002", createProduct("PROD-002", "Product 2")),
      arguments("PROD-456", createProduct("PROD-456", "Product 456")),
      arguments("NEW-001", createProduct("NEW-001", "New Product")),
      arguments("CACHE-MISS", createProduct("CACHE-MISS", "Cache Miss Product")));
  }

  private static Stream<Arguments> sortByMetricsWeightsCacheHitParams() {
    return Stream.of(
      arguments(new AppliedWeights(0.5, 0.3, 0.1, 0.1), 0L, 10L,
        List.of(createProduct("P1", "Product 1"), createProduct("P2", "Product 2"))),
      arguments(new AppliedWeights(0.25, 0.25, 0.25, 0.25), 10L, 20L,
        List.of(createProduct("P3", "Product 3"), createProduct("P4", "Product 4"), createProduct("P5", "Product 5"))),
      arguments(new AppliedWeights(1.0, 0.0, 0.0, 0.0), 0L, 5L,
        List.of(createProduct("P6", "Product 6"))),
      arguments(new AppliedWeights(0.0, 1.0, 0.0, 0.0), 5L, 15L,
        List.of(createProduct("P7", "Product 7"), createProduct("P8", "Product 8"))));
  }

  private static Stream<Arguments> sortByMetricsWeightsCacheMissParams() {
    return Stream.of(
      arguments(new AppliedWeights(0.4, 0.3, 0.2, 0.1), 0L, 10L,
        List.of(createProduct("PM1", "Product M1"), createProduct("PM2", "Product M2"))),
      arguments(new AppliedWeights(0.2, 0.2, 0.3, 0.3), 20L, 30L,
        List.of(createProduct("PM3", "Product M3"))),
      arguments(new AppliedWeights(0.6, 0.2, 0.1, 0.1), 0L, 50L,
        List.of(createProduct("PM4", "Product M4"), createProduct("PM5", "Product M5"), createProduct("PM6", "Product M6"))),
      arguments(new AppliedWeights(0.1, 0.1, 0.4, 0.4), 100L, 110L,
        List.of(createProduct("PM7", "Product M7"))));
  }

  private static Stream<Arguments> appliedWeightsParams() {
    return Stream.of(
      arguments(new AppliedWeights(0.5, 0.3, 0.1, 0.1), 0L, 10L,
        "su:0.5:st:0.3:pm:0.1:ds:0.1:off:0:lim:10"),
      arguments(new AppliedWeights(0.25, 0.25, 0.25, 0.25), 10L, 20L,
        "su:0.25:st:0.25:pm:0.25:ds:0.25:off:10:lim:20"),
      arguments(new AppliedWeights(1.0, 0.0, 0.0, 0.0), 5L, 15L,
        "su:1.0:st:0.0:pm:0.0:ds:0.0:off:5:lim:15"),
      arguments(new AppliedWeights(0.0, 0.0, 0.0, 1.0), 100L, 200L,
        "su:0.0:st:0.0:pm:0.0:ds:1.0:off:100:lim:200"));
  }

  private static Product createProduct(final String internalId, final String name) {
    return new Product("id-" + internalId, internalId, name, "Category", 100,
      Map.of("warehouse1", 50), 0.15, 30);
  }

  @ParameterizedTest
  @MethodSource("findByInternalIdCacheHitParams")
  @DisplayName("Should return product from cache when cache hit occurs")
  void shouldReturnProductFromCacheWhenCacheHitOccurs(final String internalId, final Product cachedProduct) {
    // Given
    when(this.cachePort.get("findByInternalId", internalId, Product.class)).thenReturn(Mono.just(cachedProduct));

    // When & Then
    this.cachedProductRepositoryDecorator.findByInternalId(internalId).as(StepVerifier::create)
      .expectNext(cachedProduct)
      .verifyComplete();

    verify(this.cachePort).get("findByInternalId", internalId, Product.class);
    verifyNoMoreInteractions(this.cachePort);
    verifyNoInteractions(this.delegate);
  }

  @ParameterizedTest
  @MethodSource("findByInternalIdCacheMissParams")
  @DisplayName("Should fetch from delegate and cache when cache miss occurs")
  void shouldFetchFromDelegateAndCacheWhenCacheMissOccurs(final String internalId, final Product dbProduct) {
    // Given
    when(this.cachePort.get("findByInternalId", internalId, Product.class)).thenReturn(Mono.empty());
    when(this.delegate.findByInternalId(internalId)).thenReturn(Mono.just(dbProduct));
    when(this.cachePort.put("findByInternalId", internalId, dbProduct)).thenReturn(Mono.empty());

    // When & Then
    this.cachedProductRepositoryDecorator.findByInternalId(internalId).as(StepVerifier::create)
      .expectNext(dbProduct)
      .verifyComplete();

    verify(this.cachePort).get("findByInternalId", internalId, Product.class);
    verify(this.delegate).findByInternalId(internalId);
    verify(this.cachePort).put("findByInternalId", internalId, dbProduct);
    verifyNoMoreInteractions(this.cachePort, this.delegate);
  }

  @Test
  @DisplayName("Should return empty when cache miss and delegate returns empty")
  void shouldReturnEmptyWhenCacheMissAndDelegateReturnsEmpty() {
    // Given
    final var internalId = "NON-EXISTENT";
    when(this.cachePort.get("findByInternalId", internalId, Product.class)).thenReturn(Mono.empty());
    when(this.delegate.findByInternalId(internalId)).thenReturn(Mono.empty());

    // When & Then
    this.cachedProductRepositoryDecorator.findByInternalId(internalId).as(StepVerifier::create)
      .verifyComplete();

    verify(this.cachePort).get("findByInternalId", internalId, Product.class);
    verify(this.delegate).findByInternalId(internalId);
    verify(this.cachePort, never()).put(anyString(), anyString(), any());
    verifyNoMoreInteractions(this.cachePort, this.delegate);
  }

  @Test
  @DisplayName("Should not cache when delegate returns error")
  void shouldNotCacheWhenDelegateReturnsError() {
    // Given
    final var internalId = "ERROR-PRODUCT";
    final var expectedException = new RuntimeException("Database error");
    when(this.cachePort.get("findByInternalId", internalId, Product.class)).thenReturn(Mono.empty());
    when(this.delegate.findByInternalId(internalId)).thenReturn(Mono.error(expectedException));

    // When & Then
    this.cachedProductRepositoryDecorator.findByInternalId(internalId).as(StepVerifier::create)
      .expectError(RuntimeException.class)
      .verify();

    verify(this.cachePort).get("findByInternalId", internalId, Product.class);
    verify(this.delegate).findByInternalId(internalId);
    verify(this.cachePort, never()).put(anyString(), anyString(), any());
    verifyNoMoreInteractions(this.cachePort, this.delegate);
  }

  @Test
  @DisplayName("Should propagate cache get error")
  void shouldPropagateCacheGetError() {
    // Given
    final var internalId = "CACHE-ERROR";
    final var expectedException = new RuntimeException("Cache connection error");
    when(this.cachePort.get("findByInternalId", internalId, Product.class)).thenReturn(Mono.error(expectedException));

    // When & Then
    this.cachedProductRepositoryDecorator.findByInternalId(internalId).as(StepVerifier::create)
      .expectError(RuntimeException.class)
      .verify();

    verify(this.cachePort).get("findByInternalId", internalId, Product.class);
    verifyNoMoreInteractions(this.cachePort);
    verifyNoInteractions(this.delegate);
  }

  @Test
  @DisplayName("Should continue when cache put fails but delegate succeeds")
  void shouldContinueWhenCachePutFailsButDelegateSucceeds() {
    // Given
    final var internalId = "PUT-FAIL";
    final var dbProduct = createProduct(internalId, "Product with cache put failure");
    when(this.cachePort.get("findByInternalId", internalId, Product.class)).thenReturn(Mono.empty());
    when(this.delegate.findByInternalId(internalId)).thenReturn(Mono.just(dbProduct));
    when(this.cachePort.put("findByInternalId", internalId, dbProduct))
      .thenReturn(Mono.error(new RuntimeException("Cache put error")));

    // When & Then
    this.cachedProductRepositoryDecorator.findByInternalId(internalId).as(StepVerifier::create)
      .expectError(RuntimeException.class)
      .verify();

    verify(this.cachePort).get("findByInternalId", internalId, Product.class);
    verify(this.delegate).findByInternalId(internalId);
    verify(this.cachePort).put("findByInternalId", internalId, dbProduct);
    verifyNoMoreInteractions(this.cachePort, this.delegate);
  }

  @ParameterizedTest
  @MethodSource("sortByMetricsWeightsCacheHitParams")
  @DisplayName("Should return sorted products from cache when cache hit occurs")
  void shouldReturnSortedProductsFromCacheWhenCacheHitOccurs(final AppliedWeights weights, final long offset,
                                                             final long limit, final List<Product> cachedProducts) {
    // Given
    when(this.cachePort.getList(eq("sortedProducts"), anyString(), eq(Product.class)))
      .thenReturn(Mono.just(cachedProducts));

    // When & Then
    this.cachedProductRepositoryDecorator.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectNextSequence(cachedProducts)
      .verifyComplete();

    verify(this.cachePort).getList(eq("sortedProducts"), anyString(), eq(Product.class));
    verifyNoMoreInteractions(this.cachePort);
    verifyNoInteractions(this.delegate);
  }

  @ParameterizedTest
  @MethodSource("sortByMetricsWeightsCacheMissParams")
  @DisplayName("Should fetch from delegate and cache when sorted products cache miss occurs")
  void shouldFetchFromDelegateAndCacheWhenSortedProductsCacheMissOccurs(final AppliedWeights weights,
                                                                        final long offset, final long limit,
                                                                        final List<Product> dbProducts) {
    // Given
    when(this.cachePort.getList(eq("sortedProducts"), anyString(), eq(Product.class)))
      .thenReturn(Mono.just(Collections.emptyList()));
    when(this.delegate.sortByMetricsWeights(weights, offset, limit))
      .thenReturn(Flux.fromIterable(dbProducts));
    when(this.cachePort.putList(eq("sortedProducts"), anyString(), eq(dbProducts)))
      .thenReturn(Mono.empty());

    // When & Then
    this.cachedProductRepositoryDecorator.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectNextSequence(dbProducts)
      .verifyComplete();

    verify(this.cachePort).getList(eq("sortedProducts"), anyString(), eq(Product.class));
    verify(this.delegate).sortByMetricsWeights(weights, offset, limit);
    verify(this.cachePort).putList(eq("sortedProducts"), anyString(), eq(dbProducts));
    verifyNoMoreInteractions(this.cachePort, this.delegate);
  }

  @Test
  @DisplayName("Should propagate delegate error when sorting")
  void shouldPropagateDelegateErrorWhenSorting() {
    // Given
    final var weights = new AppliedWeights(0.5, 0.3, 0.1, 0.1);
    final long offset = 0L;
    final long limit = 10L;
    final var expectedException = new RuntimeException("Database sorting error");
    when(this.cachePort.getList(eq("sortedProducts"), anyString(), eq(Product.class)))
      .thenReturn(Mono.just(Collections.emptyList()));
    when(this.delegate.sortByMetricsWeights(weights, offset, limit))
      .thenReturn(Flux.error(expectedException));

    // When & Then
    this.cachedProductRepositoryDecorator.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectError(RuntimeException.class)
      .verify();

    verify(this.cachePort).getList(eq("sortedProducts"), anyString(), eq(Product.class));
    verify(this.delegate).sortByMetricsWeights(weights, offset, limit);
    verify(this.cachePort, never()).putList(anyString(), anyString(), any());
    verifyNoMoreInteractions(this.cachePort, this.delegate);
  }

  @Test
  @DisplayName("Should propagate cache getList error when sorting")
  void shouldPropagateCacheGetListErrorWhenSorting() {
    // Given
    final var weights = new AppliedWeights(0.5, 0.3, 0.1, 0.1);
    final long offset = 0L;
    final long limit = 10L;
    final var expectedException = new RuntimeException("Cache getList error");
    when(this.cachePort.getList(eq("sortedProducts"), anyString(), eq(Product.class)))
      .thenReturn(Mono.error(expectedException));

    // When & Then
    this.cachedProductRepositoryDecorator.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectError(RuntimeException.class)
      .verify();

    verify(this.cachePort).getList(eq("sortedProducts"), anyString(), eq(Product.class));
    verifyNoMoreInteractions(this.cachePort);
    verifyNoInteractions(this.delegate);
  }

  @Test
  @DisplayName("Should handle cache putList failure after delegate success")
  void shouldHandleCachePutListFailureAfterDelegateSuccess() {
    // Given
    final var weights = new AppliedWeights(0.5, 0.3, 0.1, 0.1);
    final long offset = 0L;
    final long limit = 10L;
    final var products = List.of(createProduct("P1", "Product 1"));
    when(this.cachePort.getList(eq("sortedProducts"), anyString(), eq(Product.class)))
      .thenReturn(Mono.just(Collections.emptyList()));
    when(this.delegate.sortByMetricsWeights(weights, offset, limit))
      .thenReturn(Flux.fromIterable(products));
    when(this.cachePort.putList(eq("sortedProducts"), anyString(), eq(products)))
      .thenReturn(Mono.error(new RuntimeException("Cache putList error")));

    // When & Then
    this.cachedProductRepositoryDecorator.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectError(RuntimeException.class)
      .verify();

    verify(this.cachePort).getList(eq("sortedProducts"), anyString(), eq(Product.class));
    verify(this.delegate).sortByMetricsWeights(weights, offset, limit);
    verify(this.cachePort).putList(eq("sortedProducts"), anyString(), eq(products));
    verifyNoMoreInteractions(this.cachePort, this.delegate);
  }

  @ParameterizedTest
  @MethodSource("appliedWeightsParams")
  @DisplayName("Should generate correct cache key for different weights and pagination")
  void shouldGenerateCorrectCacheKeyForDifferentWeightsAndPagination(final AppliedWeights weights,
                                                                     final long offset, final long limit,
                                                                     final String expectedKeyPattern) {
    // Given
    final var products = List.of(createProduct("P1", "Product 1"));
    when(this.cachePort.getList("sortedProducts", expectedKeyPattern, Product.class)).thenReturn(Mono.just(products));

    // When & Then
    this.cachedProductRepositoryDecorator.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectNextSequence(products)
      .verifyComplete();

    verify(this.cachePort).getList("sortedProducts", expectedKeyPattern, Product.class);
    verifyNoMoreInteractions(this.cachePort);
    verifyNoInteractions(this.delegate);
  }

  @Test
  @DisplayName("Should handle single product in sorted results")
  void shouldHandleSingleProductInSortedResults() {
    // Given
    final var weights = new AppliedWeights(0.5, 0.3, 0.1, 0.1);
    final long offset = 0L;
    final long limit = 1L;
    final var product = createProduct("SINGLE", "Single Product");
    when(this.cachePort.getList(eq("sortedProducts"), anyString(), eq(Product.class)))
      .thenReturn(Mono.just(Collections.emptyList()));
    when(this.delegate.sortByMetricsWeights(weights, offset, limit))
      .thenReturn(Flux.just(product));
    when(this.cachePort.putList(eq("sortedProducts"), anyString(), eq(List.of(product))))
      .thenReturn(Mono.empty());

    // When & Then
    this.cachedProductRepositoryDecorator.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectNext(product)
      .verifyComplete();

    verify(this.cachePort).getList(eq("sortedProducts"), anyString(), eq(Product.class));
    verify(this.delegate).sortByMetricsWeights(weights, offset, limit);
    verify(this.cachePort).putList(eq("sortedProducts"), anyString(), eq(List.of(product)));
    verifyNoMoreInteractions(this.cachePort, this.delegate);
  }

  @Test
  @DisplayName("Should handle large product list in sorted results")
  void shouldHandleLargeProductListInSortedResults() {
    // Given
    final var weights = new AppliedWeights(0.5, 0.3, 0.1, 0.1);
    final long offset = 0L;
    final long limit = 100L;
    final var products = Stream.iterate(1, n -> n + 1)
      .limit(100)
      .map(n -> createProduct("P" + n, "Product " + n))
      .toList();
    when(this.cachePort.getList(eq("sortedProducts"), anyString(), eq(Product.class)))
      .thenReturn(Mono.just(Collections.emptyList()));
    when(this.delegate.sortByMetricsWeights(weights, offset, limit))
      .thenReturn(Flux.fromIterable(products));
    when(this.cachePort.putList(eq("sortedProducts"), anyString(), eq(products)))
      .thenReturn(Mono.empty());

    // When & Then
    this.cachedProductRepositoryDecorator.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectNextSequence(products)
      .verifyComplete();

    verify(this.cachePort).getList(eq("sortedProducts"), anyString(), eq(Product.class));
    verify(this.delegate).sortByMetricsWeights(weights, offset, limit);
    verify(this.cachePort).putList(eq("sortedProducts"), anyString(), eq(products));
    verifyNoMoreInteractions(this.cachePort, this.delegate);
  }

  @Test
  @DisplayName("Should use different cache keys for different weights")
  void shouldUseDifferentCacheKeysForDifferentWeights() {
    // Given
    final var weights1 = new AppliedWeights(0.5, 0.3, 0.1, 0.1);
    final var weights2 = new AppliedWeights(0.3, 0.5, 0.1, 0.1);
    final long offset = 0L;
    final long limit = 10L;
    final var products = List.of(createProduct("P1", "Product 1"));

    when(this.cachePort.getList(eq("sortedProducts"), anyString(), eq(Product.class)))
      .thenReturn(Mono.just(products));

    // When & Then
    this.cachedProductRepositoryDecorator.sortByMetricsWeights(weights1, offset, limit).as(StepVerifier::create)
      .expectNextSequence(products)
      .verifyComplete();

    this.cachedProductRepositoryDecorator.sortByMetricsWeights(weights2, offset, limit).as(StepVerifier::create)
      .expectNextSequence(products)
      .verifyComplete();

    verify(this.cachePort).getList("sortedProducts", "su:0.5:st:0.3:pm:0.1:ds:0.1:off:0:lim:10", Product.class);
    verify(this.cachePort).getList("sortedProducts", "su:0.3:st:0.5:pm:0.1:ds:0.1:off:0:lim:10", Product.class);
    verifyNoMoreInteractions(this.cachePort);
    verifyNoInteractions(this.delegate);
  }

  @Test
  @DisplayName("Should use different cache keys for different offset and limit")
  void shouldUseDifferentCacheKeysForDifferentOffsetAndLimit() {
    // Given
    final var weights = new AppliedWeights(0.5, 0.3, 0.1, 0.1);
    final var products = List.of(createProduct("P1", "Product 1"));

    when(this.cachePort.getList(eq("sortedProducts"), anyString(), eq(Product.class)))
      .thenReturn(Mono.just(products));

    // When & Then
    this.cachedProductRepositoryDecorator.sortByMetricsWeights(weights, 0L, 10L).as(StepVerifier::create)
      .expectNextSequence(products)
      .verifyComplete();

    this.cachedProductRepositoryDecorator.sortByMetricsWeights(weights, 10L, 20L).as(StepVerifier::create)
      .expectNextSequence(products)
      .verifyComplete();

    verify(this.cachePort).getList("sortedProducts", "su:0.5:st:0.3:pm:0.1:ds:0.1:off:0:lim:10", Product.class);
    verify(this.cachePort).getList("sortedProducts", "su:0.5:st:0.3:pm:0.1:ds:0.1:off:10:lim:20", Product.class);
    verifyNoMoreInteractions(this.cachePort);
    verifyNoInteractions(this.delegate);
  }

  @Test
  @DisplayName("Should handle extreme weight values")
  void shouldHandleExtremeWeightValues() {
    // Given
    final var weights = new AppliedWeights(0.0, 0.0, 0.0, 0.0);
    final long offset = 0L;
    final long limit = 10L;
    final var products = List.of(createProduct("P1", "Product 1"));
    when(this.cachePort.getList("sortedProducts", "su:0.0:st:0.0:pm:0.0:ds:0.0:off:0:lim:10", Product.class))
      .thenReturn(Mono.just(products));

    // When & Then
    this.cachedProductRepositoryDecorator.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectNextSequence(products)
      .verifyComplete();

    verify(this.cachePort).getList("sortedProducts", "su:0.0:st:0.0:pm:0.0:ds:0.0:off:0:lim:10", Product.class);
    verifyNoMoreInteractions(this.cachePort);
    verifyNoInteractions(this.delegate);
  }

  @Test
  @DisplayName("Should handle maximum weight values")
  void shouldHandleMaximumWeightValues() {
    // Given
    final var weights = new AppliedWeights(1.0, 1.0, 1.0, 1.0);
    final long offset = Long.MAX_VALUE;
    final long limit = Long.MAX_VALUE;
    final var products = List.of(createProduct("P1", "Product 1"));
    final var expectedKey = "su:1.0:st:1.0:pm:1.0:ds:1.0:off:%d:lim:%d".formatted(Long.MAX_VALUE, Long.MAX_VALUE);
    when(this.cachePort.getList("sortedProducts", expectedKey, Product.class)).thenReturn(Mono.just(products));

    // When & Then
    this.cachedProductRepositoryDecorator.sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectNextSequence(products)
      .verifyComplete();

    verify(this.cachePort).getList("sortedProducts", expectedKey, Product.class);
    verifyNoMoreInteractions(this.cachePort);
    verifyNoInteractions(this.delegate);
  }
}
