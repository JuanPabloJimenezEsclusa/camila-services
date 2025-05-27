package com.camila.api.product.infrastructure.adapter.input.cache;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.usecase.ProductUseCase;
import com.camila.api.product.infrastructure.adapter.input.cache.config.CacheConfig;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@DisplayName("[IT][CachedProductDecorator] Cached Product Decorator test")
class CachedProductDecoratorITCase {

  @Configuration
  @Import(CacheConfig.class)
  static class TestConfig {
    @Bean
    @Qualifier("mockProductUseCase")
    public ProductUseCase mockProductUseCase() {
      return mock(ProductUseCase.class);
    }

    @Bean
    @Primary
    public ProductUseCase cachedDecorator(@Qualifier("mockProductUseCase") final ProductUseCase mockProductUseCase) {
      return new CachedProductDecorator(mockProductUseCase);
    }
  }

  @Autowired
  private ProductUseCase cachedDecorator;

  @Autowired
  @Qualifier("mockProductUseCase")
  private ProductUseCase mockProductUseCase;

  @Autowired
  private CacheManager cacheManager;

  @AfterEach
  void tearDown() {
    reset(mockProductUseCase);
    cacheManager.getCacheNames().forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
  }

  private static Stream<Arguments> productIdScenarios() {
    // scenario, internalId
    return Stream.of(
      Arguments.of("Standard ID", "123"),
      Arguments.of("ID with special chars", "ABC-123"),
      Arguments.of("ID with leading zeros", "00000"),
      Arguments.of("Long numeric ID", "9999999999")
    );
  }

  private static Stream<Arguments> sortParametersScenarios() {
    // scenario, requestParams
    return Stream.of(
      Arguments.of("Empty map", Collections.emptyMap()),
      Arguments.of("Single parameter", Map.of("salesUnits", "1.0")),
      Arguments.of("Multiple parameters", Map.of("salesUnits", "0.5", "stock", "0.3", "profitMargin", "0.2")),
      Arguments.of("With pagination", Map.of("salesUnits", "0.5", "page", "1", "size", "10"))
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("productIdScenarios")
  @DisplayName("Should cache findByInternalId results")
  void shouldCacheFindByInternalIdResults(final String scenario, final String productId) {
    // Given
    final var product = Instancio.of(Product.class).create();
    when(mockProductUseCase.findByInternalId(productId)).thenReturn(Mono.just(product));

    // When & Then
    cachedDecorator.findByInternalId(productId).as(StepVerifier::create).expectNext(product).verifyComplete();
    cachedDecorator.findByInternalId(productId).as(StepVerifier::create).expectNext(product).verifyComplete();

    verify(mockProductUseCase, times(1)).findByInternalId(productId);
    verifyNoMoreInteractions(mockProductUseCase);
  }

  @ParameterizedTest
  @EmptySource
  @ValueSource(strings = {"  "})
  @DisplayName("Should cache findByInternalId results with blank IDs")
  void shouldCacheFindByInternalIdResultsWithBlankIds(final String productId) {
    // Given
    when(mockProductUseCase.findByInternalId(productId)).thenReturn(Mono.empty());

    // When & Then
    cachedDecorator.findByInternalId(productId).as(StepVerifier::create).verifyComplete();
    cachedDecorator.findByInternalId(productId).as(StepVerifier::create).verifyComplete();

    verify(mockProductUseCase, times(1)).findByInternalId(productId);
    verifyNoMoreInteractions(mockProductUseCase);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("sortParametersScenarios")
  @DisplayName("Should cache sortByMetricsWeights results")
  void shouldCacheSortByMetricsWeightsResults(final String scenario, Map<String, String> requestParams) {
    // Given
    final var product = Instancio.of(Product.class).create();
    when(mockProductUseCase.sortByMetricsWeights(requestParams)).thenReturn(Flux.just(product));

    // When & Then
    cachedDecorator.sortByMetricsWeights(requestParams).as(StepVerifier::create).expectNext(product).verifyComplete();
    cachedDecorator.sortByMetricsWeights(requestParams).as(StepVerifier::create).expectNext(product).verifyComplete();

    verify(mockProductUseCase, times(1)).sortByMetricsWeights(requestParams);
    verifyNoMoreInteractions(mockProductUseCase);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("productIdScenarios")
  @DisplayName("Should not cache results for different product IDs")
  void shouldNotCacheResultsForDifferentProductIds(final String scenario, final String productId) {
    // Given
    final var product1 = Instancio.of(Product.class).create();
    final var product2 = Instancio.of(Product.class).create();
    final var differentId = productId + "-different";
    when(mockProductUseCase.findByInternalId(productId)).thenReturn(Mono.just(product1));
    when(mockProductUseCase.findByInternalId(differentId)).thenReturn(Mono.just(product2));

    // When & Then
    cachedDecorator.findByInternalId(productId).as(StepVerifier::create).expectNext(product1).verifyComplete();
    cachedDecorator.findByInternalId(differentId).as(StepVerifier::create).expectNext(product2).verifyComplete();

    verify(mockProductUseCase).findByInternalId(productId);
    verify(mockProductUseCase).findByInternalId(differentId);
    verifyNoMoreInteractions(mockProductUseCase);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("sortParametersScenarios")
  @DisplayName("Should not cache results for different sort parameters")
  void shouldNotCacheResultsForDifferentSortParameters(final String scenario, Map<String, String> requestParams) {
    // Given
    final var product1 = Instancio.of(Product.class).create();
    final var product2 = Instancio.of(Product.class).create();
    final var differentParams = Map.of("size", "1000");
    when(mockProductUseCase.sortByMetricsWeights(requestParams)).thenReturn(Flux.just(product1));
    when(mockProductUseCase.sortByMetricsWeights(differentParams)).thenReturn(Flux.just(product2));

    // When & Then
    cachedDecorator.sortByMetricsWeights(requestParams).as(StepVerifier::create).expectNext(product1).verifyComplete();
    cachedDecorator.sortByMetricsWeights(differentParams).as(StepVerifier::create).expectNext(product2).verifyComplete();

    verify(mockProductUseCase).sortByMetricsWeights(requestParams);
    verify(mockProductUseCase).sortByMetricsWeights(differentParams);
    verifyNoMoreInteractions(mockProductUseCase);
  }

  @Test
  @DisplayName("Should verify cache eviction timeout")
  void shouldVerifyCacheEvictionTimeout() {
    // Given
    final var productId = "test-product";
    final var product = Instancio.of(Product.class).create();
    when(mockProductUseCase.findByInternalId(productId)).thenReturn(Mono.just(product));

    // When & Then - First call caches result
    cachedDecorator.findByInternalId(productId).as(StepVerifier::create).expectNext(product).verifyComplete();

    // When & Then - After eviction, should call delegate again
    Objects.requireNonNull(cacheManager.getCache("findByInternalId")).evict(productId);
    cachedDecorator.findByInternalId(productId).as(StepVerifier::create).expectNext(product).verifyComplete();

    verify(mockProductUseCase, times(2)).findByInternalId(productId);
    verifyNoMoreInteractions(mockProductUseCase);
  }
}
