package com.camila.gateway.presentation;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * The type Rate limit config.
 */
@Configuration
class RateLimitConfig {

  /**
   * User key resolver key resolver.
   *
   * @return the key resolver
   */
  @Bean
  KeyResolver userKeyResolver() {
    return _ -> Mono.just("1");
  }
}
