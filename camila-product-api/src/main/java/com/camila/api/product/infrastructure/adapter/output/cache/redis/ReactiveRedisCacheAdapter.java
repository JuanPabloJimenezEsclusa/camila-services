package com.camila.api.product.infrastructure.adapter.output.cache.redis;

import com.camila.api.product.domain.port.CachePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import reactor.core.publisher.Mono;

/**
 * Reactive cache adapter backed by Spring Reactive Redis operations.
 */
@Slf4j
public record ReactiveRedisCacheAdapter(
  ReactiveRedisOperations<String, Object> redisOps) implements CachePort {

  @Override
  public <T> Mono<T> get(final String cacheName, final String key, final Class<T> type) {
    final String fullKey = key(cacheName, key);
    return redisOps.opsForValue().get(fullKey)
      .doOnNext(v -> log.info("Retrieved value from Redis cache: {} = {}", fullKey, v))
      .flatMap(v -> {
        try {
          return Mono.just(type.cast(v));
        } catch (ClassCastException e) {
          log.warn("Error retrieving from cache: {}", e.getMessage());
          return Mono.empty();
        }
      })
      .doOnError(e -> log.error("Error retrieving from Redis cache: {} for key: {}", e.getMessage(), fullKey));
  }

  @Override
  public Mono<Void> put(final String cacheName, final String key, final Object value) {
    final String fullKey = key(cacheName, key);
    log.debug("Putting value in Redis cache: {} = {}", fullKey, value);
    return redisOps.opsForValue().set(fullKey, value)
      .doOnError(e -> log.error("Error putting to Redis cache: {} for key: {}", e.getMessage(), fullKey))
      .then();
  }

  private String key(final String cacheName, final String key) {
    return "%s:%s".formatted(cacheName, key);
  }
}
