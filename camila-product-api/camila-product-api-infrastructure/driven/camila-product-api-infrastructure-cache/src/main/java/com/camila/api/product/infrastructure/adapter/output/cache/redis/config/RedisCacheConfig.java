package com.camila.api.product.infrastructure.adapter.output.cache.redis.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.camila.api.product.domain.port.CachePort;
import com.camila.api.product.infrastructure.adapter.output.cache.redis.ReactiveRedisCacheAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Configuration(proxyBeanMethods = false)
@EnableCaching
@Profile("dev|local-compose")
public class RedisCacheConfig {

  private static GenericJacksonJsonRedisSerializer jsonSerializer() {
    final var typeValidator = BasicPolymorphicTypeValidator.builder()
      .allowIfSubType("com.camila.api.product.")
      .allowIfSubType("java.util.")
      .build();
    return GenericJacksonJsonRedisSerializer.builder()
      .enableDefaultTyping(typeValidator)
      .build();
  }

  @Bean
  public LettuceConnectionFactory redisConnectionFactory(
    @Value("${spring.data.redis.host:localhost}") final String host,
    @Value("${spring.data.redis.port:6379}") final int port,
    @Value("${spring.data.redis.username:default}") final String username,
    @Value("${spring.data.redis.password:camila}") final String password
  ) {
    final var conf = new RedisStandaloneConfiguration(host, port);
    conf.setUsername(username);
    conf.setPassword(RedisPassword.of(password));
    return new LettuceConnectionFactory(conf);
  }

  @Bean
  public CacheManager cacheManager(final LettuceConnectionFactory connectionFactory,
                                   @Value("${spring.cache.cache-names:findByInternalId,sortedProducts}") final String cacheNames,
                                   @Value("${spring.cache.redis.ttl-seconds:60}") final long ttlSeconds,
                                   @Value("${spring.cache.redis.key-prefix:cache:}") final String keyPrefix) {

    final var cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
      .entryTtl(Duration.ofSeconds(ttlSeconds))
      .prefixCacheNameWith(keyPrefix)
      .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
        jsonSerializer()));

    final var initialNames = Arrays.stream(cacheNames.split(","))
      .map(String::trim)
      .filter(s -> !s.isEmpty())
      .collect(Collectors.toSet());

    return RedisCacheManager.builder(connectionFactory)
      .cacheDefaults(cacheConfig)
      .initialCacheNames(initialNames)
      .build();
  }

  @Bean
  public ReactiveRedisOperations<String, Object> reactiveRedisOperations(final LettuceConnectionFactory connectionFactory) {
    final var keySerializer = new StringRedisSerializer();
    final var valueSerializer = jsonSerializer();
    final RedisSerializationContext<String, Object> context = RedisSerializationContext
      .<String, Object>newSerializationContext(valueSerializer)
      .key(keySerializer)
      .value(valueSerializer)
      .hashKey(keySerializer)
      .hashValue(valueSerializer)
      .build();

    return new ReactiveRedisTemplate<>(connectionFactory, context);
  }

  @Bean
  public CachePort reactiveRedisCacheAdapter(final ReactiveRedisOperations<String, Object> reactiveRedisOperations) {
    return new ReactiveRedisCacheAdapter(reactiveRedisOperations);
  }
}
