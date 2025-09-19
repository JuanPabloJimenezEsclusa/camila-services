package com.camila.api.product.infrastructure.adapter.output.cache;

import static org.mockito.Mockito.mock;

import com.camila.api.product.domain.port.ProductRepository;
import com.camila.api.product.infrastructure.adapter.output.cache.config.CaffeineCacheConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@DisplayName("[IT][CachedProductDecorator] Caffeine Cached Product Decorator test")
class CaffeineCachedProductDecoratorITCase extends AbstractCachedProductDecoratorITCase {

  @Autowired
  private ProductRepository cachedDecorator;

  @Autowired
  @Qualifier("mockProductRepository")
  private ProductRepository mockProductRepository;

  @Autowired
  private CacheManager cacheManager;

  @Override
  protected ProductRepository cachedDecorator() {
    return cachedDecorator;
  }

  @Override
  protected ProductRepository mockProductRepository() {
    return mockProductRepository;
  }

  @Override
  protected CacheManager cacheManager() {
    return cacheManager;
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
    @Primary
    public ProductRepository cachedDecorator(@Qualifier("mockProductRepository") final ProductRepository mockProductRepository) {
      return new CachedProductRepositoryDecorator(mockProductRepository);
    }
  }
}
