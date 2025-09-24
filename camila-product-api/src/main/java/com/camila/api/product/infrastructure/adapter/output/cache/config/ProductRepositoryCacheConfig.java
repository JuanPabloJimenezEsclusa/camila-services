package com.camila.api.product.infrastructure.adapter.output.cache.config;

import com.camila.api.product.domain.port.CachePort;
import com.camila.api.product.domain.port.ProductRepository;
import com.camila.api.product.infrastructure.adapter.output.cache.CachedProductRepositoryDecorator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration that enables caching and wraps the active ProductRepository
 * implementation with a caching decorator in the output/infrastructure layer.
 */
@Configuration
@EnableCaching
public class ProductRepositoryCacheConfig {

  /**
   * Register the cached product repository and prefer any available CachePort
   * (for example, ReactiveRedisCacheAdapter or ReactiveCaffeineCacheAdapter) injected by
   * profile-specific configuration.
   *
   * @param productRepositories the product repositories
   * @param reactiveCacheServices the reactive cache services
   * @return the product repository
   */
  @Bean(name = "cachedProductRepository")
  public ProductRepository cachedProductRepository(final ObjectProvider<ProductRepository> productRepositories,
                                                    final ObjectProvider<CachePort> reactiveCacheServices) {
    final var delegateRepository = productRepositories.stream()
      .filter(repository -> !(repository instanceof CachedProductRepositoryDecorator))
      .findFirst()
      .orElseThrow();
    final var cacheService = reactiveCacheServices.stream()
      .findFirst()
      .orElseThrow();

    return new CachedProductRepositoryDecorator(delegateRepository, cacheService);
  }
}
