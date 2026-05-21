package com.camila.api.product.domain.port;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("[UT][CachePort] Cache Port Unit Tests")
class CachePortUnitTest {

  private static Stream<Arguments> getListSuccessParams() {
    return Stream.of(
      arguments("products", "product-1", List.of("item1", "item2")),
      arguments("users", "user-123", List.of("user1")),
      arguments("cache1", "key1", List.of("a", "b", "c")),
      arguments("cache2", "key2", List.of())
    );
  }

  private static Stream<Arguments> getListWithTypeCastingParams() {
    return Stream.of(
      arguments("products", "product-1", List.of("item1", "item2"), String.class),
      arguments("users", "user-123", List.of("user1", "user2", "user3"), String.class)
    );
  }

  private static Stream<Arguments> getListEmptyParams() {
    return Stream.of(
      arguments("products", "product-1", "not-a-list", String.class),
      arguments("users", "user-123", 42, Integer.class),
      arguments("cache1", "key1", true, Boolean.class)
    );
  }

  private static Stream<Arguments> getListEmptyMonoParams() {
    return Stream.of(
      arguments("products", "product-1", String.class),
      arguments("users", "user-123", Integer.class),
      arguments("cache1", "key1", Boolean.class)
    );
  }

  private static Stream<Arguments> putListParams() {
    return Stream.of(
      arguments("products", "product-1", List.of("item1", "item2")),
      arguments("users", "user-123", List.of("user1")),
      arguments("cache1", "key1", List.of()),
      arguments("cache2", "key2", List.of("a", "b", "c", "d"))
    );
  }

  @Mock
  private CachePort cachePort;

  @ParameterizedTest
  @MethodSource("getListSuccessParams")
  @DisplayName("Should get list from cache successfully")
  void shouldGetListFromCacheSuccessfully(final String cacheName, final String key, final List<String> list) {
    // Given
    when(cachePort.get(cacheName, key, Object.class)).thenReturn(Mono.just(list));
    when(cachePort.getList(cacheName, key, String.class)).thenCallRealMethod();

    // When & Then
    cachePort.getList(cacheName, key, String.class)
      .as(StepVerifier::create)
      .expectNext(list)
      .verifyComplete();

    verify(cachePort).getList(cacheName, key, String.class);
    verify(cachePort).get(cacheName, key, Object.class);
    verifyNoMoreInteractions(cachePort);
  }

  @ParameterizedTest
  @MethodSource("getListWithTypeCastingParams")
  @DisplayName("Should get list and cast elements to correct type")
  void shouldGetListAndCastElementsToCorrectType(final String cacheName, final String key, final List<Object> rawList, final Class<String> type) {
    // Given
    when(cachePort.get(cacheName, key, Object.class)).thenReturn(Mono.just(rawList));
    when(cachePort.getList(cacheName, key, type)).thenCallRealMethod();

    // When & Then
    cachePort.getList(cacheName, key, type)
      .as(StepVerifier::create)
      .assertNext(result -> {
        assertThat(result).hasSameSizeAs(rawList);
        assertThat(result).allMatch(item -> item.getClass().equals(type));
      })
      .verifyComplete();

    verify(cachePort).getList(cacheName, key, type);
    verify(cachePort).get(cacheName, key, Object.class);
    verifyNoMoreInteractions(cachePort);
  }

  @ParameterizedTest
  @MethodSource("getListEmptyParams")
  @DisplayName("Should return empty mono when cached value is not a list")
  void shouldReturnEmptyMonoWhenCachedValueIsNotAList(final String cacheName, final String key, final Object nonListValue, final Class<?> type) {
    // Given
    when(cachePort.get(cacheName, key, Object.class)).thenReturn(Mono.just(nonListValue));
    when(cachePort.getList(cacheName, key, type)).thenCallRealMethod();

    // When & Then
    cachePort.getList(cacheName, key, type)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(cachePort).getList(cacheName, key, type);
    verify(cachePort).get(cacheName, key, Object.class);
    verifyNoMoreInteractions(cachePort);
  }

  @ParameterizedTest
  @MethodSource("getListEmptyMonoParams")
  @DisplayName("Should return empty mono when cache returns empty")
  void shouldReturnEmptyMonoWhenCacheReturnsEmpty(final String cacheName, final String key, final Class<?> type) {
    // Given
    when(cachePort.get(cacheName, key, Object.class)).thenReturn(Mono.empty());
    when(cachePort.getList(cacheName, key, type)).thenCallRealMethod();

    // When & Then
    cachePort.getList(cacheName, key, type)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(cachePort).getList(cacheName, key, type);
    verify(cachePort).get(cacheName, key, Object.class);
    verifyNoMoreInteractions(cachePort);
  }

  @ParameterizedTest
  @MethodSource("putListParams")
  @DisplayName("Should put list in cache successfully")
  void shouldPutListInCacheSuccessfully(final String cacheName, final String key, final List<String> list) {
    // Given
    when(cachePort.put(cacheName, key, list)).thenReturn(Mono.empty());
    when(cachePort.putList(cacheName, key, list)).thenCallRealMethod();

    // When & Then
    cachePort.putList(cacheName, key, list)
      .as(StepVerifier::create)
      .verifyComplete();

    verify(cachePort).putList(cacheName, key, list);
    verify(cachePort).put(cacheName, key, list);
    verifyNoMoreInteractions(cachePort);
  }

  @Test
  @DisplayName("Should handle empty list correctly")
  void shouldHandleEmptyListCorrectly() {
    // Given
    final var cacheName = "products";
    final var key = "product-list";
    final List<String> emptyList = List.of();
    when(cachePort.get(cacheName, key, Object.class)).thenReturn(Mono.just(emptyList));
    when(cachePort.getList(cacheName, key, String.class)).thenCallRealMethod();

    // When & Then
    cachePort.getList(cacheName, key, String.class)
      .as(StepVerifier::create)
      .expectNext(emptyList)
      .verifyComplete();

    verify(cachePort).getList(cacheName, key, String.class);
    verify(cachePort).get(cacheName, key, Object.class);
    verifyNoMoreInteractions(cachePort);
  }

  @Test
  @DisplayName("Should handle list with single element")
  void shouldHandleListWithSingleElement() {
    // Given
    final var cacheName = "products";
    final var key = "product-single";
    final List<String> singleElementList = List.of("item1");
    when(cachePort.get(cacheName, key, Object.class)).thenReturn(Mono.just(singleElementList));
    when(cachePort.getList(cacheName, key, String.class)).thenCallRealMethod();

    // When & Then
    cachePort.getList(cacheName, key, String.class)
      .as(StepVerifier::create)
      .assertNext(result -> {
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo("item1");
      })
      .verifyComplete();

    verify(cachePort).getList(cacheName, key, String.class);
    verify(cachePort).get(cacheName, key, Object.class);
    verifyNoMoreInteractions(cachePort);
  }

  @Test
  @DisplayName("Should handle list with multiple elements")
  void shouldHandleListWithMultipleElements() {
    // Given
    final var cacheName = "products";
    final var key = "product-multiple";
    final List<String> multipleElementsList = List.of("item1", "item2", "item3");
    when(cachePort.get(cacheName, key, Object.class)).thenReturn(Mono.just(multipleElementsList));
    when(cachePort.getList(cacheName, key, String.class)).thenCallRealMethod();

    // When & Then
    cachePort.getList(cacheName, key, String.class)
      .as(StepVerifier::create)
      .assertNext(result -> {
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("item1", "item2", "item3");
      })
      .verifyComplete();

    verify(cachePort).getList(cacheName, key, String.class);
    verify(cachePort).get(cacheName, key, Object.class);
    verifyNoMoreInteractions(cachePort);
  }

  @Test
  @DisplayName("Should handle list with mutable ArrayList")
  void shouldHandleListWithMutableArrayList() {
    // Given
    final var cacheName = "products";
    final var key = "product-arraylist";
    final List<Object> mutableList = new ArrayList<>();
    mutableList.add("item1");
    mutableList.add("item2");
    when(cachePort.get(cacheName, key, Object.class)).thenReturn(Mono.just(mutableList));
    when(cachePort.getList(cacheName, key, String.class)).thenCallRealMethod();

    // When & Then
    cachePort.getList(cacheName, key, String.class)
      .as(StepVerifier::create)
      .assertNext(result -> {
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("item1", "item2");
      })
      .verifyComplete();

    verify(cachePort).getList(cacheName, key, String.class);
    verify(cachePort).get(cacheName, key, Object.class);
    verifyNoMoreInteractions(cachePort);
  }
}

