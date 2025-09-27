package com.camila.api.product.infrastructure.adapter.output.cache.caffeine;

import static org.mockito.Mockito.mock;

import com.camila.api.product.domain.port.CachePort;
import com.camila.api.product.domain.port.ProductRepository;
import com.camila.api.product.infrastructure.adapter.output.cache.AbstractCachedProductDecoratorITCase;
import com.camila.api.product.infrastructure.adapter.output.cache.CachedProductRepositoryDecorator;
import com.camila.api.product.infrastructure.adapter.output.cache.caffeine.config.CaffeineCacheConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@DisplayName("[IT][CachedProductDecorator] Caffeine Cached Product Decorator test")
class CaffeineCachedProductDecoratorITCase extends AbstractCachedProductDecoratorITCase {

  @Autowired
  @Qualifier("cachedProductRepositoryDecorator")
  private ProductRepository cachedProductRepositoryDecorator;

  @Autowired
  @Qualifier("mockProductRepository")
  private ProductRepository mockProductRepository;

  @Autowired
  private CacheManager cacheManager;

  @Override
  protected ProductRepository cachedProductRepositoryDecorator() {
    return this.cachedProductRepositoryDecorator;
  }

  @Override
  protected ProductRepository mockProductRepository() {
    return this.mockProductRepository;
  }

  @Override
  protected CacheManager cacheManager() {
    return this.cacheManager;
  }

  @Configuration
  @Import(CaffeineCacheConfig.class)
  static class TestConfig {
    @Bean
    @Qualifier("mockProductRepository")
    public ProductRepository mockProductRepository() {
      return mock(ProductRepository.class);
    }

    @Bean
    public ProductRepository cachedProductRepositoryDecorator(
      @Qualifier("mockProductRepository") final ProductRepository mockProductRepository,
      @Qualifier("reactiveCaffeineCacheAdapter") final CachePort reactiveCaffeineCacheAdapter) {
      return new CachedProductRepositoryDecorator(mockProductRepository, reactiveCaffeineCacheAdapter);
    }
  }
}
