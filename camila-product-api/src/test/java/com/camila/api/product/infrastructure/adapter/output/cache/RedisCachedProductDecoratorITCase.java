package com.camila.api.product.infrastructure.adapter.output.cache;

import static org.mockito.Mockito.mock;

import com.camila.api.product.domain.port.ProductRepository;
import com.camila.api.product.infrastructure.adapter.output.cache.config.RedisCacheConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@TestPropertySource(properties = {"spring.profiles.active=dev"})
@ExtendWith(SpringExtension.class)
@DisplayName("[IT][CachedProductDecorator] Redis Cached Product Decorator test")
class RedisCachedProductDecoratorITCase extends RedisTestContainerConfig {

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
  @Import(RedisCacheConfig.class)
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
