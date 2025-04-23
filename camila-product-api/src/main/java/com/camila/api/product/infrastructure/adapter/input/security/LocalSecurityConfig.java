package com.camila.api.product.infrastructure.adapter.input.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * The type Local security config.
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Profile("default|loc|local-compose|int|pro")
// It must be public to use in unit tests
public class LocalSecurityConfig {

  /**
   * Security web filter chain.
   *
   * @param http the http
   * @return the security web filter chain
   */
  @Bean
  SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
    return http
      .cors(ServerHttpSecurity.CorsSpec::disable)
      .csrf(ServerHttpSecurity.CsrfSpec::disable)
      .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
      .build();
  }
}
