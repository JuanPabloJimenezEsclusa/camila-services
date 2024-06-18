package com.camila.api.product.framework.adapter.input.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Collections;

/**
 * The type Cors config.
 */
@Configuration
@Profile("dev|pre")
// https://docs.spring.io/spring-security/reference/reactive/integrations/cors.html
class CorsConfig {
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

  private static UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource() {
    var corsConfig = new CorsConfiguration();
    corsConfig.setAllowedOrigins(Collections.singletonList("*"));
    corsConfig.setAllowedMethods(Collections.singletonList("*"));
    corsConfig.setAllowedHeaders(Collections.singletonList("*"));
    corsConfig.setAllowCredentials(true);
    corsConfig.setAllowPrivateNetwork(true);
    corsConfig.setMaxAge(3600L);

    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);
    return source;
  }
}
