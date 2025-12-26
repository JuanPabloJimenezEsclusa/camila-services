package com.camila.api.product.domain.port;

import java.util.ArrayList;
import java.util.List;

import reactor.core.publisher.Mono;

/**
 * The interface Cache port.
 */
public interface CachePort {

  /**
   * Get.
   *
   * @param <T>       the type parameter
   * @param cacheName the cache name
   * @param key       the key
   * @param type      the type
   * @return the T mono
   */
  <T> Mono<T> get(String cacheName, String key, Class<T> type);

  /**
   * Put.
   *
   * @param cacheName the cache name
   * @param key       the key
   * @param value     the value
   * @return Void
   */
  Mono<Void> put(String cacheName, String key, Object value);

  /**
   * Gets list.
   *
   * @param <T>       the type parameter
   * @param cacheName the cache name
   * @param key       the key
   * @param type      the type
   * @return the list
   */
  default <T> Mono<List<T>> getList(final String cacheName, final String key, final Class<T> type) {
    return this.get(cacheName, key, Object.class).flatMap(o -> {
      if (!(o instanceof List<?> raw)) {
        return Mono.empty();
      }
      final List<T> typed = new ArrayList<>(raw.size());
      for (final var item : raw) {
        typed.add(type.cast(item));
      }
      return Mono.just(typed);
    });
  }

  /**
   * Put list.
   *
   * @param <T>       the type parameter
   * @param cacheName the cache name
   * @param key       the key
   * @param value     the value
   * @return Void
   */
  default <T> Mono<Void> putList(final String cacheName, final String key, final List<T> value) {
    return this.put(cacheName, key, value);
  }
}
