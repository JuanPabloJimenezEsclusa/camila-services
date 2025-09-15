package com.camila.api.product.infrastructure.adapter.input.cache;

import static org.mockito.Mockito.mock;

import com.camila.api.product.domain.usecase.ProductUseCase;
import com.camila.api.product.infrastructure.adapter.input.cache.config.CaffeineCacheConfig;
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
  private ProductUseCase cachedDecorator;

  @Autowired
  @Qualifier("mockProductUseCase")
  private ProductUseCase mockProductUseCase;

  @Autowired
  private CacheManager cacheManager;

  @Override
  protected ProductUseCase cachedDecorator() {
    return cachedDecorator;
  }

  @Override
  protected ProductUseCase mockProductUseCase() {
    return mockProductUseCase;
  }

  @Override
  protected CacheManager cacheManager() {
    return cacheManager;
  }

  @Configuration
  @Import(CaffeineCacheConfig.class)
  static class TestConfig {
    @Bean
    @Qualifier("mockProductUseCase")
    public ProductUseCase mockProductUseCase() {
      return mock(ProductUseCase.class);
    }

    @Bean
    @Primary
    public ProductUseCase cachedDecorator(@Qualifier("mockProductUseCase") final ProductUseCase mockProductUseCase) {
      return new CachedProductDecorator(mockProductUseCase);
    }
  }
}
