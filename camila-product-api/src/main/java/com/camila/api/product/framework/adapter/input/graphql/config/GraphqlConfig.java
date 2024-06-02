package com.camila.api.product.framework.adapter.input.graphql.config;

import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * The type Graphql config.
 */
@Configuration
class GraphqlConfig {
  /**
   * Runtime wiring configurer runtime wiring configurer.
   *
   * @return the runtime wiring configurer
   */
  @Bean
  public RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return wiringBuilder -> wiringBuilder.scalar(ExtendedScalars.Json);
  }
}
