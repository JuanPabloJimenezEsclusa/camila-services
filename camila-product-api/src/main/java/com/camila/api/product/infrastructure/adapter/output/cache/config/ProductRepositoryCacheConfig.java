package com.camila.api.product.infrastructure.adapter.output.cache.config;

import com.camila.api.product.domain.port.ProductRepository;
import com.camila.api.product.infrastructure.adapter.output.cache.CachedProductRepositoryDecorator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Primary;

/**
 * Configuration that enables caching and wraps the active ProductRepository
 * implementation with a caching decorator in the output/infrastructure layer.
 */
@Configuration
@EnableCaching
public class ProductRepositoryCacheConfig {

  private static final String NO_IMPLEMENTATION_FOUND_TO_WRAP_CACHE =
    "No ProductRepository implementation found to wrap with cache";

  /**
   * Cached product repository.
   *
   * @param repositories the repositories
   * @return the product repository
   */
  @Bean(name = "cachedProductRepository")
  @Primary
  public ProductRepository cachedProductRepository(final ObjectProvider<ProductRepository> repositories) {
    final var delegate = repositories.stream()
      .filter(repo -> !(repo instanceof CachedProductRepositoryDecorator))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException(NO_IMPLEMENTATION_FOUND_TO_WRAP_CACHE));

    return new CachedProductRepositoryDecorator(delegate);
  }
}
