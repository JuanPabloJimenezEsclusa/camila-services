package com.camila.api.product.infrastructure.adapter.input.cache.config;

import java.time.Duration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up caching in the application.
 * This class uses Caffeine as the caching provider and defines
 * the cache behavior and manager.
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfig {

  /**
   * Configures the Caffeine cache with specific settings.
   *
   * @return a Caffeine instance configured with:
   *     - Expiration of cache entries 1 minute after write.
   *     - Maximum size of 100 entries.
   */
  @Bean
  public Caffeine<Object, Object> caffeineConfig() {
    return Caffeine.newBuilder()
      .expireAfterWrite(Duration.ofMinutes(1L))
      .initialCapacity(100)
      .maximumSize(10_000);
  }

  /**
   * Creates a CacheManager bean that uses the configured Caffeine instance.
   *
   * @param caffeine the Caffeine instance to be used by the cache manager.
   * @return a CacheManager configured to use Caffeine for caching.
   */
  @Bean
  public CacheManager cacheManager(final Caffeine<Object, Object> caffeine) {
    final var cacheManager = new CaffeineCacheManager();
    cacheManager.setCaffeine(caffeine);
    cacheManager.setAllowNullValues(true);
    // Enable async cache mode for reactive types
    cacheManager.setAsyncCacheMode(true);
    return cacheManager;
  }
}
