package com.camila.gateway.infrastructure.adapter.input.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

  @Bean
  SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
    return http
      .cors(ServerHttpSecurity.CorsSpec::disable)
      .csrf(ServerHttpSecurity.CsrfSpec::disable)
      .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
      .build();
  }
}
