package com.camila.api.product.framework.adapter.input.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Profile("default|loc")
public class LocalSecurityConfig {

  @Bean
  SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http
      .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
      .cors(ServerHttpSecurity.CorsSpec::disable)
      .csrf(ServerHttpSecurity.CsrfSpec::disable)
      .build();
  }
}
