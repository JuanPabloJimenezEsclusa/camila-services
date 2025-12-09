package com.camila.api.product.infrastructure.adapter.input.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Jackson ObjectMapper. Provides an ObjectMapper bean
 * for JSON serialization/deserialization.
 */
@Configuration
public class JacksonConfig {

  /**
   * Creates an ObjectMapper bean if one is not already present. Configures the
   * ObjectMapper with Java 8 time support and proper date formatting.
   *
   * @return the configured ObjectMapper instance
   */
  @Bean
  @ConditionalOnMissingBean
  public ObjectMapper objectMapper() {
    final var mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }
}
