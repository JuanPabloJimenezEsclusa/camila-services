package com.camila.api.product.infrastructure.adapter.output.cache.caffeine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("[UT][ReactiveCaffeineCacheAdapter] Reactive Caffeine Cache Adapter Unit Tests")
class ReactiveCaffeineCacheAdapterUnitTest {

  @Mock
  private CacheManager cacheManager;

  @Mock
  private Cache cache;

  @Mock
  private Cache.ValueWrapper valueWrapper;

  private static Stream<Arguments> getSuccessParams() {
    return Stream.of(
      arguments("products", "product-1", "Product One", String.class),
      arguments("users", "user-123", 42, Integer.class),
      arguments("cache1", "key1", 100L, Long.class),
      arguments("cache2", "key2", true, Boolean.class)
    );
  }

  private static Stream<Arguments> getCompletionStageParams() {
    return Stream.of(
      arguments("products", "product-1", "Product One", String.class),
      arguments("users", "user-123", 42, Integer.class),
      arguments("cache1", "key1", 100L, Long.class)
    );
  }

  private static Stream<Arguments> getCacheNameAndKeyParams() {
    return Stream.of(
      arguments("products", "product-1", String.class),
      arguments("users", "user-123", Integer.class),
      arguments("cache1", "key1", Long.class),
      arguments("cache2", "key2", Boolean.class)
    );
  }

  private static Stream<Arguments> getExceptionParams() {
    return Stream.of(
      arguments("products", "product-1", String.class, new RuntimeException("Cache error")),
      arguments("users", "user-123", Integer.class, new ClassCastException("Cast error")),
      arguments("cache1", "key1", Long.class, new IllegalStateException("State error"))
    );
  }

  private static Stream<Arguments> putParams() {
    return Stream.of(
      arguments("products", "product-1", "Product One"),
      arguments("users", "user-123", 42),
      arguments("cache1", "key1", 100L),
      arguments("cache2", "key2", true),
      arguments("cache3", "key3", null)
    );
  }

  @Test
  @DisplayName("Should create adapter with valid cache manager")
  void shouldCreateAdapterWithValidCacheManager() {
    // Given
    // When
    final var adapter = new ReactiveCaffeineCacheAdapter(cacheManager);

    // Then
    assertNotNull(adapter);
    assertEquals(cacheManager, adapter.cacheManager());
    verifyNoInteractions(cacheManager);
  }

  @ParameterizedTest
  @MethodSource("getSuccessParams")
  @DisplayName("Should get value from cache successfully")
  <T> void shouldGetValueFromCacheSuccessfully(final String cacheName, final String key, final T value, final Class<T> type) {
    // Given
    final var adapter = new ReactiveCaffeineCacheAdapter(cacheManager);
    when(cacheManager.getCache(cacheName)).thenReturn(cache);
    when(cache.get(key)).thenReturn(valueWrapper);
    when(valueWrapper.get()).thenReturn(value);

    // When & Then
    adapter.get(cacheName, key, type)
      .as(StepVerifier::create)
      .expectNext(value)
      .verifyComplete();

    verify(cacheManager).getCache(cacheName);
    verify(cache).get(key);
    verify(valueWrapper).get();
    verifyNoMoreInteractions(cacheManager, cache, valueWrapper);
  }

  @ParameterizedTest
  @MethodSource("getCompletionStageParams")
  @DisplayName("Should get value from cache when wrapped in CompletionStage")
  <T> void shouldGetValueFromCacheWhenWrappedInCompletionStage(final String cacheName, final String key, final T unwrappedValue, final Class<T> type) {
    // Given
    final var adapter = new ReactiveCaffeineCacheAdapter(cacheManager);
    final CompletionStage<T> completionStage = CompletableFuture.completedFuture(unwrappedValue);
    when(cacheManager.getCache(cacheName)).thenReturn(cache);
    when(cache.get(key)).thenReturn(valueWrapper);
    when(valueWrapper.get()).thenReturn(completionStage);

    // When & Then
    adapter.get(cacheName, key, type)
      .as(StepVerifier::create)
      .expectNext(unwrappedValue)
      .verifyComplete();

    verify(cacheManager).getCache(cacheName);
    verify(cache).get(key);
    verify(valueWrapper).get();
    verifyNoMoreInteractions(cacheManager, cache, valueWrapper);
  }

  @ParameterizedTest
  @MethodSource("getCacheNameAndKeyParams")
  @DisplayName("Should return empty mono when cache value wrapper is null")
  void shouldReturnEmptyMonoWhenCacheValueWrapperIsNull(final String cacheName, final String key, final Class<?> type) {
    // Given
    final var adapter = new ReactiveCaffeineCacheAdapter(cacheManager);
    when(cacheManager.getCache(cacheName)).thenReturn(cache);
    when(cache.get(key)).thenReturn(null);

    // When & Then
    adapter.get(cacheName, key, type)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(cacheManager).getCache(cacheName);
    verify(cache).get(key);
    verifyNoMoreInteractions(cacheManager, cache);
    verifyNoInteractions(valueWrapper);
  }

  @ParameterizedTest
  @MethodSource("getCacheNameAndKeyParams")
  @DisplayName("Should return empty mono when cache value is null")
  void shouldReturnEmptyMonoWhenCacheValueIsNull(final String cacheName, final String key, final Class<?> type) {
    // Given
    final var adapter = new ReactiveCaffeineCacheAdapter(cacheManager);
    when(cacheManager.getCache(cacheName)).thenReturn(cache);
    when(cache.get(key)).thenReturn(valueWrapper);
    when(valueWrapper.get()).thenReturn(null);

    // When & Then
    adapter.get(cacheName, key, type)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(cacheManager).getCache(cacheName);
    verify(cache).get(key);
    verify(valueWrapper).get();
    verifyNoMoreInteractions(cacheManager, cache, valueWrapper);
  }

  @ParameterizedTest
  @MethodSource("getExceptionParams")
  @DisplayName("Should return empty mono when exception occurs")
  void shouldReturnEmptyMonoWhenExceptionOccurs(final String cacheName, final String key, final Class<?> type, final Exception exception) {
    // Given
    final var adapter = new ReactiveCaffeineCacheAdapter(cacheManager);
    when(cacheManager.getCache(cacheName)).thenReturn(cache);
    when(cache.get(key)).thenThrow(exception);

    // When & Then
    adapter.get(cacheName, key, type)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(cacheManager).getCache(cacheName);
    verify(cache).get(key);
    verifyNoMoreInteractions(cacheManager, cache);
    verifyNoInteractions(valueWrapper);
  }

  @ParameterizedTest
  @MethodSource("putParams")
  @DisplayName("Should put value in cache successfully")
  void shouldPutValueInCacheSuccessfully(final String cacheName, final String key, final Object value) {
    // Given
    final var adapter = new ReactiveCaffeineCacheAdapter(cacheManager);
    when(cacheManager.getCache(cacheName)).thenReturn(cache);

    // When & Then
    adapter.put(cacheName, key, value)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(cacheManager).getCache(cacheName);
    verify(cache).put(key, value);
    verifyNoMoreInteractions(cacheManager, cache);
    verifyNoInteractions(valueWrapper);
  }

  @ParameterizedTest
  @MethodSource("getCacheNameAndKeyParams")
  @DisplayName("Should return empty mono when cache manager returns null")
  void shouldReturnEmptyMonoWhenCacheManagerReturnsNull(final String cacheName, final String key, final Class<?> type) {
    // Given
    final var adapter = new ReactiveCaffeineCacheAdapter(cacheManager);
    when(cacheManager.getCache(cacheName)).thenReturn(null);

    // When & Then
    adapter.get(cacheName, key, type)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(cacheManager).getCache(cacheName);
    verifyNoMoreInteractions(cacheManager);
    verifyNoInteractions(cache, valueWrapper);
  }
}

