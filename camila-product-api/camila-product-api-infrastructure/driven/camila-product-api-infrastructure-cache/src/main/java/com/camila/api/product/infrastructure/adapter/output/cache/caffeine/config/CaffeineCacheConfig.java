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

@Configuration(proxyBeanMethods = false)
@EnableCaching
@Profile("!dev&&!local-compose")
public class CaffeineCacheConfig {

  @Bean
  public Caffeine<Object, Object> caffeineConfig(
    @Value("${spring.cache.caffeine.spec:initialCapacity=100,maximumSize=10000,expireAfterAccess=60s}") final String spec) {
    return Caffeine.from(spec);
  }

  @Bean
  public CacheManager cacheManager(final Caffeine<Object, Object> caffeine) {
    final var cacheManager = new CaffeineCacheManager();
    cacheManager.setCaffeine(caffeine);
    cacheManager.setAllowNullValues(true);
    // Enable async cache mode for reactive types
    cacheManager.setAsyncCacheMode(true);
    return cacheManager;
  }

  @Bean
  public CachePort reactiveCaffeineCacheAdapter(final CacheManager cacheManager) {
    return new ReactiveCaffeineCacheAdapter(cacheManager);
  }
}
