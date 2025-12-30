package com.camila.api.product.infrastructure.adapter.output.cache.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("[UT][ReactiveRedisCacheAdapter] Reactive Redis Cache Adapter Unit Tests")
class ReactiveRedisCacheAdapterUnitTest {

  @Mock
  private ReactiveRedisOperations<String, Object> redisOps;

  @Mock
  private ReactiveValueOperations<String, Object> valueOps;

  private static Stream<Arguments> getSuccessParams() {
    return Stream.of(
      arguments("products", "product-1", "Product One", String.class),
      arguments("users", "user-123", 42, Integer.class),
      arguments("cache1", "key1", 100L, Long.class),
      arguments("cache2", "key2", true, Boolean.class)
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

  private static Stream<Arguments> getCastErrorParams() {
    return Stream.of(
      arguments("products", "product-1", 123, String.class),
      arguments("users", "user-123", "not-an-integer", Integer.class),
      arguments("cache1", "key1", "not-a-long", Long.class)
    );
  }

  private static Stream<Arguments> getErrorParams() {
    return Stream.of(
      arguments("products", "product-1", String.class, new RuntimeException("Redis connection error")),
      arguments("users", "user-123", Integer.class, new IllegalStateException("Redis timeout")),
      arguments("cache1", "key1", Long.class, new RuntimeException("Network error"))
    );
  }

  private static Stream<Arguments> putSuccessParams() {
    return Stream.of(
      arguments("products", "product-1", "Product One"),
      arguments("users", "user-123", 42),
      arguments("cache1", "key1", 100L),
      arguments("cache2", "key2", true),
      arguments("cache3", "key3", null)
    );
  }

  private static Stream<Arguments> putErrorParams() {
    return Stream.of(
      arguments("products", "product-1", "Product One", new RuntimeException("Redis write error")),
      arguments("users", "user-123", 42, new IllegalStateException("Redis full")),
      arguments("cache1", "key1", 100L, new RuntimeException("Connection lost"))
    );
  }

  @Test
  @DisplayName("Should create adapter with valid redis operations")
  void shouldCreateAdapterWithValidRedisOperations() {
    // Given
    // When
    final var adapter = new ReactiveRedisCacheAdapter(redisOps);

    // Then
    assertNotNull(adapter);
    assertEquals(redisOps, adapter.redisOps());
    verifyNoInteractions(redisOps, valueOps);
  }

  @ParameterizedTest
  @MethodSource("getSuccessParams")
  @DisplayName("Should get value from redis cache successfully")
  <T> void shouldGetValueFromRedisCacheSuccessfully(final String cacheName, final String key, final T value, final Class<T> type) {
    // Given
    final var adapter = new ReactiveRedisCacheAdapter(redisOps);
    final var fullKey = cacheName + ":" + key;
    when(redisOps.opsForValue()).thenReturn(valueOps);
    when(valueOps.get(fullKey)).thenReturn(Mono.just(value));

    // When & Then
    adapter.get(cacheName, key, type)
      .as(StepVerifier::create)
      .expectNext(value)
      .verifyComplete();

    verify(redisOps).opsForValue();
    verify(valueOps).get(fullKey);
    verifyNoMoreInteractions(redisOps, valueOps);
  }

  @ParameterizedTest
  @MethodSource("getCacheNameAndKeyParams")
  @DisplayName("Should return empty mono when redis returns empty")
  void shouldReturnEmptyMonoWhenRedisReturnsEmpty(final String cacheName, final String key, final Class<?> type) {
    // Given
    final var adapter = new ReactiveRedisCacheAdapter(redisOps);
    final var fullKey = cacheName + ":" + key;
    when(redisOps.opsForValue()).thenReturn(valueOps);
    when(valueOps.get(fullKey)).thenReturn(Mono.empty());

    // When & Then
    adapter.get(cacheName, key, type)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(redisOps).opsForValue();
    verify(valueOps).get(fullKey);
    verifyNoMoreInteractions(redisOps, valueOps);
  }

  @ParameterizedTest
  @MethodSource("getCastErrorParams")
  @DisplayName("Should return empty mono when class cast exception occurs")
  void shouldReturnEmptyMonoWhenClassCastExceptionOccurs(final String cacheName, final String key, final Object value, final Class<?> type) {
    // Given
    final var adapter = new ReactiveRedisCacheAdapter(redisOps);
    final var fullKey = cacheName + ":" + key;
    when(redisOps.opsForValue()).thenReturn(valueOps);
    when(valueOps.get(fullKey)).thenReturn(Mono.just(value));

    // When & Then
    adapter.get(cacheName, key, type)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(redisOps).opsForValue();
    verify(valueOps).get(fullKey);
    verifyNoMoreInteractions(redisOps, valueOps);
  }

  @ParameterizedTest
  @MethodSource("getErrorParams")
  @DisplayName("Should propagate error when redis get operation fails")
  void shouldPropagateErrorWhenRedisGetOperationFails(final String cacheName, final String key, final Class<?> type, final Throwable error) {
    // Given
    final var adapter = new ReactiveRedisCacheAdapter(redisOps);
    final var fullKey = cacheName + ":" + key;
    when(redisOps.opsForValue()).thenReturn(valueOps);
    when(valueOps.get(fullKey)).thenReturn(Mono.error(error));

    // When & Then
    adapter.get(cacheName, key, type)
      .as(StepVerifier::create)
      .expectError(error.getClass())
      .verify();

    verify(redisOps).opsForValue();
    verify(valueOps).get(fullKey);
    verifyNoMoreInteractions(redisOps, valueOps);
  }

  @ParameterizedTest
  @MethodSource("putSuccessParams")
  @DisplayName("Should put value in redis cache successfully")
  void shouldPutValueInRedisCacheSuccessfully(final String cacheName, final String key, final Object value) {
    // Given
    final var adapter = new ReactiveRedisCacheAdapter(redisOps);
    final var fullKey = cacheName + ":" + key;
    when(redisOps.opsForValue()).thenReturn(valueOps);
    when(valueOps.set(fullKey, value)).thenReturn(Mono.just(true));

    // When & Then
    adapter.put(cacheName, key, value)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(redisOps).opsForValue();
    verify(valueOps).set(fullKey, value);
    verifyNoMoreInteractions(redisOps, valueOps);
  }

  @ParameterizedTest
  @MethodSource("putSuccessParams")
  @DisplayName("Should complete when redis set returns false")
  void shouldCompleteWhenRedisSetReturnsFalse(final String cacheName, final String key, final Object value) {
    // Given
    final var adapter = new ReactiveRedisCacheAdapter(redisOps);
    final var fullKey = cacheName + ":" + key;
    when(redisOps.opsForValue()).thenReturn(valueOps);
    when(valueOps.set(fullKey, value)).thenReturn(Mono.just(false));

    // When & Then
    adapter.put(cacheName, key, value)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(redisOps).opsForValue();
    verify(valueOps).set(fullKey, value);
    verifyNoMoreInteractions(redisOps, valueOps);
  }

  @ParameterizedTest
  @MethodSource("putErrorParams")
  @DisplayName("Should propagate error when redis put operation fails")
  void shouldPropagateErrorWhenRedisPutOperationFails(final String cacheName, final String key, final Object value, final Throwable error) {
    // Given
    final var adapter = new ReactiveRedisCacheAdapter(redisOps);
    final var fullKey = cacheName + ":" + key;
    when(redisOps.opsForValue()).thenReturn(valueOps);
    when(valueOps.set(fullKey, value)).thenReturn(Mono.error(error));

    // When & Then
    adapter.put(cacheName, key, value)
      .as(StepVerifier::create)
      .expectError(error.getClass())
      .verify();

    verify(redisOps).opsForValue();
    verify(valueOps).set(fullKey, value);
    verifyNoMoreInteractions(redisOps, valueOps);
  }
}

