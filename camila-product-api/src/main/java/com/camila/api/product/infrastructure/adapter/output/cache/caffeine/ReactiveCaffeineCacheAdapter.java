package com.camila.api.product.infrastructure.adapter.output.cache.caffeine;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

import com.camila.api.product.domain.port.CachePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import reactor.core.publisher.Mono;

/**
 * Reactive cache adapter implementation backed by Spring CacheManager + Caffeine.
 */
@Slf4j
public record ReactiveCaffeineCacheAdapter(
  CacheManager cacheManager) implements CachePort {

  @Override
  public <T> Mono<T> get(final String cacheName, final String key, final Class<T> type) {
    final var cache = resolve(cacheName);
    try {
      final var cacheWrapper = cache.get(key);
      if (cacheWrapper == null) {
        return Mono.empty();
      }
      final var value = cacheWrapper.get();
      if (value == null) {
        return Mono.empty();
      }
      if (value instanceof CompletionStage<?> cs) {
        return Mono.fromCompletionStage(cs)
          .flatMap(unwrapped -> Mono.just(type.cast(unwrapped)));
      }
      return Mono.just(type.cast(value));
    } catch (Exception e) {
      if (log.isWarnEnabled()) {
        log.warn("Error retrieving from cache: {}", e.getMessage());
      }
      return Mono.empty();
    }
  }

  @Override
  public Mono<Void> put(final String cacheName, final String key, final Object value) {
    resolve(cacheName).put(key, value);
    return Mono.empty();
  }

  private Cache resolve(final String cacheName) {
    return Objects.requireNonNull(cacheManager.getCache(cacheName));
  }
}
