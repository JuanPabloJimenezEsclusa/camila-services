package com.camila.api.product.infrastructure.adapter.input.rest.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

/**
 * Configuration class for Jackson ObjectMapper. Provides an ObjectMapper bean
 * for JSON serialization/deserialization. In Jackson 3.x (Spring Boot 4.x),
 * Java time support is included by default and ISO-8601 format is the standard.
 */
@Configuration
public class JacksonConfig {

  /**
   * Creates an ObjectMapper bean if one is not already present.
   *
   * @return the configured ObjectMapper instance
   */
  @Bean
  @ConditionalOnMissingBean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
}
