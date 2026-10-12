package com.camila.gateway.infrastructure.adapter.input.rest.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
// https://docs.spring.io/spring-security/reference/reactive/integrations/cors.html
class CorsConfig {

  private static UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource() {
    final var corsConfig = new CorsConfiguration();
    corsConfig.setAllowedOriginPatterns(Collections.singletonList("*"));
    corsConfig.setAllowedMethods(Collections.singletonList("*"));
    corsConfig.setAllowedHeaders(Collections.singletonList("*"));
    corsConfig.setAllowCredentials(true);
    corsConfig.setAllowPrivateNetwork(true);
    corsConfig.setMaxAge(3600L);

    final var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);
    return source;
  }

  @Bean
  CorsWebFilter corsWebFilter() {
    return new CorsWebFilter(urlBasedCorsConfigurationSource());
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    return urlBasedCorsConfigurationSource();
  }
}
