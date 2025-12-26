package com.camila.api.product.infrastructure.adapter.output.cache.redis;

import static org.mockito.Mockito.mock;

import com.camila.api.product.domain.port.CachePort;
import com.camila.api.product.domain.port.ProductRepository;
import com.camila.api.product.infrastructure.adapter.output.cache.AbstractCachedProductDecoratorITCase;
import com.camila.api.product.infrastructure.adapter.output.cache.CachedProductRepositoryDecorator;
import com.camila.api.product.infrastructure.adapter.output.cache.redis.config.RedisCacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@TestPropertySource(properties = {"spring.profiles.active=dev"})
@ExtendWith(SpringExtension.class)
@DisplayName("[IT][CachedProductDecorator] Redis Cached Product Decorator test")
class RedisCachedProductDecoratorITCase extends AbstractCachedProductDecoratorITCase {

  static {
    RedisContainerConfig.init();
  }

  @Autowired
  @Qualifier("cachedProductRepositoryDecorator")
  private ProductRepository cachedProductRepositoryDecorator;
  @Autowired
  @Qualifier("mockProductRepository")
  private ProductRepository mockProductRepository;
  @Autowired
  private CacheManager cacheManager;
  @Autowired
  private ReactiveRedisOperations<String, Object> reactiveRedisOperations;

  @BeforeEach
  void setUp() {
    this.cacheManager().getCacheNames().forEach(name -> this.reactiveRedisOperations.keys(name + "*")
      .flatMap(key -> this.reactiveRedisOperations.delete(key)).subscribe());
  }

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
  @Import({RedisCacheConfig.class})
  static class TestConfig {
    @Bean
    @Qualifier("mockProductRepository")
    public ProductRepository mockProductRepository() {
      return mock(ProductRepository.class);
    }

    @Bean
    public ProductRepository cachedProductRepositoryDecorator(
      @Qualifier("mockProductRepository") final ProductRepository mockProductRepository,
      @Qualifier("reactiveRedisCacheAdapter") final CachePort reactiveRedisCacheAdapter) {
      return new CachedProductRepositoryDecorator(mockProductRepository, reactiveRedisCacheAdapter);
    }
  }
}
