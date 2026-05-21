package com.camila.api.product.infrastructure.adapter.output.cache.caffeine.config;

import com.camila.api.product.domain.port.CachePort;
import com.camila.api.product.infrastructure.adapter.output.cache.caffeine.ReactiveCaffeineCacheAdapter;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class for setting up caching in the application.
 * This class uses Caffeine as the caching provider and defines
 * the cache behavior and manager.
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
@Profile("!dev&&!local-compose")
public class CaffeineCacheConfig {

  /**
   * Configures the Caffeine cache with specific settings.
   *
   * @return a Caffeine instance configured with:
   *     - Expiration of cache entries 1 minute after write.
   *     - Initial capacity of 100 entries.
   *     - Maximum size of 10_000 entries.
   */
  @Bean
  public Caffeine<Object, Object> caffeineConfig(
    @Value("${spring.cache.caffeine.spec:initialCapacity=100,maximumSize=10000,expireAfterAccess=60s}")
    final String spec) {
    return Caffeine.from(spec);
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

  /**
   * Reactive caffeine cache adapter.
   *
   * @param cacheManager the cache manager
   * @return the reactive cache service
   */
  @Bean
  public CachePort reactiveCaffeineCacheAdapter(final CacheManager cacheManager) {
    return new ReactiveCaffeineCacheAdapter(cacheManager);
  }
}
