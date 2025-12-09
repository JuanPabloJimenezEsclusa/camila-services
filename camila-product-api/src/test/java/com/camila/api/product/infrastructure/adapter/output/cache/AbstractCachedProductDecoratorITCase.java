package com.camila.api.product.infrastructure.adapter.output.cache;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;

import com.camila.api.product.domain.model.AppliedWeights;
import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.port.ProductRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@DisplayName("[IT][CachedProductDecorator] Cached Product Decorator test")
public abstract class AbstractCachedProductDecoratorITCase {

  protected abstract ProductRepository cachedProductRepositoryDecorator();

  protected abstract ProductRepository mockProductRepository();

  protected abstract CacheManager cacheManager();

  private static Stream<Arguments> productIdScenarios() {
    // internalId
    return Stream.of(
      arguments(named("Standard ID: 123", "123")),
      arguments(named("ID with special chars: ABC-123", "ABC-123")),
      arguments(named("ID with leading zeros: 00000", "00000")),
      arguments(named("Long numeric ID: 9999999999", "9999999999")));
  }

  private static Stream<Arguments> sortParametersScenarios() {
    // weights, offset, limit
    return Stream.of(
      arguments(named("Empty map", new AppliedWeights(0f, 0f, 0f, 0f)), 0, 10),
      arguments(named("Single parameter", new AppliedWeights(1.0f, 0f, 0f, 0f)), 0, 10),
      arguments(named("Multiple parameters", new AppliedWeights(0.5f, 0.3f, 0.2f, 0f)), 0, 10),
      arguments(named("With custom pagination", new AppliedWeights(0.5f, 0f, 0f, 0f)), 1, 10));
  }

  @AfterEach
  void tearDown() {
    reset(this.mockProductRepository());
    this.cacheManager().getCacheNames()
      .forEach(name -> Optional.ofNullable(this.cacheManager().getCache(name)).ifPresent(cache -> {
        cache.invalidate();
        cache.clear();
      }));
  }

  @ParameterizedTest(name = "{index}: {0}")
  @MethodSource("productIdScenarios")
  @DisplayName("Should cache findByInternalId results")
  void shouldCacheFindByInternalIdResults(final String productId) {
    // Given
    final var product = Instancio.of(Product.class).set(field(Product::internalId), productId).create();
    when(this.mockProductRepository().findByInternalId(productId)).thenReturn(Mono.just(product));

    // When & Then
    this.cachedProductRepositoryDecorator().findByInternalId(productId).as(StepVerifier::create).expectNext(product)
      .verifyComplete();
    this.cachedProductRepositoryDecorator().findByInternalId(productId).as(StepVerifier::create).expectNext(product)
      .verifyComplete();

    verify(this.mockProductRepository()).findByInternalId(productId);
    verifyNoMoreInteractions(this.mockProductRepository());
  }

  @ParameterizedTest
  @EmptySource
  @ValueSource(strings = {"  "})
  @DisplayName("Should not cache findByInternalId results with blank IDs")
  void shouldCacheFindByInternalIdResultsWithBlankIds(final String productId) {
    // Given
    when(this.mockProductRepository().findByInternalId(productId)).thenReturn(Mono.empty());

    // When & Then
    this.cachedProductRepositoryDecorator().findByInternalId(productId).as(StepVerifier::create).verifyComplete();
    this.cachedProductRepositoryDecorator().findByInternalId(productId).as(StepVerifier::create).verifyComplete();

    verify(this.mockProductRepository(), times(2)).findByInternalId(productId);
    verifyNoMoreInteractions(this.mockProductRepository());
  }

  @ParameterizedTest(name = "{index}: {0}")
  @MethodSource("productIdScenarios")
  @DisplayName("Should not cache results for different product IDs")
  void shouldNotCacheResultsForDifferentProductIds(final String productId) {
    // Given
    final var differentId = productId + "-different";
    final var product1 = Instancio.of(Product.class).set(field(Product::internalId), productId).create();
    final var product2 = Instancio.of(Product.class).set(field(Product::internalId), differentId).create();
    when(this.mockProductRepository().findByInternalId(productId)).thenReturn(Mono.just(product1));
    when(this.mockProductRepository().findByInternalId(differentId)).thenReturn(Mono.just(product2));

    // When & Then
    this.cachedProductRepositoryDecorator().findByInternalId(productId).as(StepVerifier::create)
      .expectNext(product1).verifyComplete();
    this.cachedProductRepositoryDecorator().findByInternalId(differentId).as(StepVerifier::create)
      .expectNext(product2).verifyComplete();

    verify(this.mockProductRepository(), times(2)).findByInternalId(anyString());
    verifyNoMoreInteractions(this.mockProductRepository());
  }

  @ParameterizedTest(name = "{index}: {0}")
  @MethodSource("sortParametersScenarios")
  @DisplayName("Should cache sortByMetricsWeights results")
  void shouldCacheSortByMetricsWeightsResults(final AppliedWeights weights, final long offset, final long limit) {
    // Given
    final var product = Instancio.of(Product.class).create();
    when(this.mockProductRepository().sortByMetricsWeights(weights, offset, limit)).thenReturn(Flux.just(product));

    // When & Then
    this.cachedProductRepositoryDecorator().sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectNext(product).verifyComplete();
    this.cachedProductRepositoryDecorator().sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectNext(product).verifyComplete();

    verify(this.mockProductRepository()).sortByMetricsWeights(weights, offset, limit);
    verifyNoMoreInteractions(this.mockProductRepository());
  }

  @ParameterizedTest(name = "{index}: {0}")
  @MethodSource("sortParametersScenarios")
  @DisplayName("Should not cache results for different sort parameters")
  void shouldNotCacheResultsForDifferentSortParameters(final AppliedWeights weights, final long offset,
                                                       final long limit) {
    // Given
    final var product1 = Instancio.of(Product.class).create();
    final var product2 = Instancio.of(Product.class).create();
    final var differentAppliedWeights = new AppliedWeights(0f, 0f, 0f, 1_000f);
    when(this.mockProductRepository().sortByMetricsWeights(weights, offset, limit)).thenReturn(Flux.just(product1));
    when(this.mockProductRepository().sortByMetricsWeights(differentAppliedWeights, offset, limit))
      .thenReturn(Flux.just(product2));

    // When & Then
    this.cachedProductRepositoryDecorator().sortByMetricsWeights(weights, offset, limit).as(StepVerifier::create)
      .expectNext(product1).verifyComplete();
    this.cachedProductRepositoryDecorator().sortByMetricsWeights(differentAppliedWeights, offset, limit)
      .as(StepVerifier::create).expectNext(product2).verifyComplete();

    verify(this.mockProductRepository()).sortByMetricsWeights(weights, offset, limit);
    verify(this.mockProductRepository()).sortByMetricsWeights(differentAppliedWeights, offset, limit);
    verifyNoMoreInteractions(this.mockProductRepository());
  }
}
