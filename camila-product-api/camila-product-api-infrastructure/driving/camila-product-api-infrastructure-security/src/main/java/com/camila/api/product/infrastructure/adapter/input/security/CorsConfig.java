package com.camila.api.product.infrastructure.adapter.input.security;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * The type Cors config.
 */
@Configuration(proxyBeanMethods = false)
@Profile("dev|pre")
// https://docs.spring.io/spring-security/reference/reactive/integrations/cors.html
class CorsConfig {
  private static UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource() {
    final var corsConfig = new CorsConfiguration();
    corsConfig.setAllowedOrigins(Collections.singletonList("*"));
    corsConfig.setAllowedMethods(Collections.singletonList("*"));
    corsConfig.setAllowedHeaders(Collections.singletonList("*"));
    corsConfig.setAllowCredentials(true);
    corsConfig.setAllowPrivateNetwork(true);
    corsConfig.setMaxAge(3600L);

    final var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);
    return source;
  }

  /**
   * Cors web filter cors web filter.
   *
   * @return the cors web filter
   */
  @Bean
  CorsWebFilter corsWebFilter() {
    return new CorsWebFilter(urlBasedCorsConfigurationSource());
  }

  /**
   * Cors configuration source cors configuration source.
   *
   * @return the cors configuration source
   */
  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    return urlBasedCorsConfigurationSource();
  }
}
