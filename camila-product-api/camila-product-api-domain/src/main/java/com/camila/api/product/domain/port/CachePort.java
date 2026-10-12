package com.camila.api.product.domain.port;

import java.util.List;

import reactor.core.publisher.Mono;

public interface CachePort {

  <T> Mono<T> get(String cacheName, String key, Class<T> type);

  Mono<Void> put(String cacheName, String key, Object value);

  default <T> Mono<List<T>> getList(final String cacheName, final String key, final Class<T> type) {
    return this.get(cacheName, key, Object.class).flatMap(o -> {
      if (!(o instanceof List<?> raw)) {
        return Mono.empty();
      }

      return Mono.just(raw.stream()
        .map(type::cast)
        .toList());
    });
  }

  default <T> Mono<Void> putList(final String cacheName, final String key, final List<T> value) {
    return this.put(cacheName, key, value);
  }
}
