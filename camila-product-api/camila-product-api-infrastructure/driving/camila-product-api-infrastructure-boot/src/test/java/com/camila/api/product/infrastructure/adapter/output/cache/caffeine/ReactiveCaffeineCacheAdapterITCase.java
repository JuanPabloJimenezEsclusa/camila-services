package com.camila.api.product.infrastructure.adapter.output.cache.caffeine;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.camila.api.product.domain.port.CachePort;
import com.camila.api.product.infrastructure.adapter.output.cache.caffeine.config.CaffeineCacheConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CaffeineCacheConfig.class)
@DisplayName("[IT][ReactiveCaffeineCacheAdapter] integration tests")
class ReactiveCaffeineCacheAdapterITCase {

  private static final Object SKIP_PUT = new Object();

  @Autowired
  private CachePort reactiveCaffeineCacheAdapter;

  private static Stream<Arguments> putGetScenarios() {
    return Stream.of(
      arguments("plain-string", "k1", "hello", String.class, true, "hello"),
      arguments("completion-stage", "k2", CompletableFuture.completedFuture("async"), String.class, true, "async"),
      arguments("wrong-type", "k3", "abc", Integer.class, false, null),
      arguments("missing-key", "k4", SKIP_PUT, String.class, false, null),
      arguments("list-value", "k5", List.of(1, 2), List.class, true, List.of(1, 2)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("putGetScenarios")
  @DisplayName("Should handle put/get scenarios")
  void shouldHandlePutGetScenarios(final String name, final String key, final Object storedValue,
                                   final Class<?> requestedType, final boolean expectPresent, final Object expectedValue) {
    // Given
    final var cacheName = "test-%s".formatted(name);

    if (!Objects.equals(storedValue, SKIP_PUT)) {
      // When (put)
      this.reactiveCaffeineCacheAdapter.put(cacheName, key, storedValue).as(StepVerifier::create)
        .verifyComplete();
    }

    // Then (get)
    final Mono<Object> cacheValue = this.reactiveCaffeineCacheAdapter.get(cacheName, key,
      requestedType.asSubclass(Object.class));
    if (expectPresent) {
      cacheValue.as(StepVerifier::create).expectNextMatches(v -> Objects.equals(v, expectedValue))
        .verifyComplete();
    } else {
      cacheValue.as(StepVerifier::create).verifyComplete();
    }

    if (expectedValue instanceof List<?> expectedList) {
      // When (putList)
      this.reactiveCaffeineCacheAdapter.putList(cacheName, "%s-list".formatted(key), expectedList)
        .as(StepVerifier::create).verifyComplete();
      // Then (getList)
      this.reactiveCaffeineCacheAdapter.getList(cacheName, "%s-list".formatted(key), Integer.class)
        .as(StepVerifier::create).expectNextMatches(l -> Objects.equals(l, expectedList)).verifyComplete();
    }
  }
}
