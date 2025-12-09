package com.camila.gateway.infrastructure.adapter.input.rest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

/**
 * The type Rate limit config.
 */
@Configuration
class RateLimitConfig {

  private static final Logger log = LoggerFactory.getLogger(RateLimitConfig.class);

  private final String rlPrefix;
  private final String envName;

  RateLimitConfig(
    @Value("${rate.limiter.prefix:rl}") final String rlPrefix,
    @Value("${spring.profiles.active:default}") final String envName) {
    this.rlPrefix = rlPrefix;
    this.envName = envName;
  }

  /**
   * Resolve a key for the rate limiter following this precedence:
   * 1) X-Forwarded-For header first entry
   * 2) remote address (client IP)
   * 3) fallback to 'anonymous'
   * The final key is prefixed with the configured prefix and env so limits are per-env per-route per-user/IP
   * e.g. "rl-loc-products-user123".
   */
  @Bean
  KeyResolver userKeyResolver() {
    return exchange -> {
      final ServerHttpRequest request = exchange.getRequest();

      final var fromXff = Mono.justOrEmpty(request.getHeaders().getFirst("X-Forwarded-For"))
        .map(h -> h.split(",")[0].trim());

      final var fromRemote = Mono.justOrEmpty(request.getRemoteAddress())
        .map(addr -> addr.getAddress().getHostAddress());

      final var resolvedKey = fromXff.switchIfEmpty(fromRemote).switchIfEmpty(Mono.just("anonymous"));

      final Mono<String> routeIdMono = Mono.defer(() -> {
        final Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        final String routeId = route != null ? route.getId() : "route";
        return Mono.just(routeId);
      });

      return Mono.zip(routeIdMono, resolvedKey)
        .map(tuple -> "%s-%s-%s-%s".formatted(this.rlPrefix, this.envName, tuple.getT1(), tuple.getT2()))
        .doOnNext(k -> log.info("RateLimiter key resolved: {} for path {}", k, request.getPath()));
    };
  }
}
