package com.camila.api.product.infrastructure.adapter.output.cache.redis;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.camila.api.product.domain.port.CachePort;
import com.camila.api.product.infrastructure.adapter.output.cache.redis.config.RedisCacheConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.StepVerifier;

@SpringJUnitConfig
@TestPropertySource(properties = {"spring.profiles.active=dev"})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RedisCacheConfig.class})
@DisplayName("[IT][ReactiveRedisCacheAdapter] integration tests")
class ReactiveRedisCacheAdapterITCase {

  private static final Object SKIP_PUT = new Object();

  @Autowired
  private CachePort adapter;

  static {
    RedisContainerConfig.init();
  }

  private static Stream<Arguments> scenarios() {
    return Stream.of(
      arguments("plain-string", "k1", "hello", String.class, true, "hello"),
      arguments("wrong-type", "k2", "abc", Integer.class, false, null),
      arguments("missing-key", "k3", SKIP_PUT, String.class, false, null));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("scenarios")
  @DisplayName("Should handle get/put scenarios")
  void shouldHandleGetPutScenarios(final String name, final String key, final Object storedValue,
                                   final Class<?> requestedType, final boolean expectPresent, final Object expectedValue) {
    // Given
    final var cacheName = "rtest-" + name;
    if (!Objects.equals(storedValue, SKIP_PUT)) {
      // When (put)
      this.adapter.put(cacheName, key, storedValue).as(StepVerifier::create).verifyComplete();
    }

    // Then (get)
    final var resultMono = this.adapter.get(cacheName, key, requestedType.asSubclass(Object.class));
    if (expectPresent) {
      resultMono.as(StepVerifier::create).expectNextMatches(v -> Objects.equals(v, expectedValue))
        .verifyComplete();
    } else {
      resultMono.as(StepVerifier::create).verifyComplete();
    }

    if (expectedValue instanceof List<?> expectedList) {
      final var listKey = "%s-list".formatted(key);
      // When (putList)
      this.adapter.putList(cacheName, listKey, expectedList).as(StepVerifier::create).verifyComplete();
      // Then (getList)
      this.adapter.getList(cacheName, listKey, Integer.class).as(StepVerifier::create)
        .expectNextMatches(l -> Objects.equals(l, expectedList)).verifyComplete();
    }
  }
}
