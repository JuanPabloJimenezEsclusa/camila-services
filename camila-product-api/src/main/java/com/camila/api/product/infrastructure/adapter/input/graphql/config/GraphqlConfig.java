package com.camila.api.product.infrastructure.adapter.input.graphql.config;

import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * The type Graphql config.
 */
@Configuration(proxyBeanMethods = false)
public class GraphqlConfig {

  /**
   * Configures the runtime wiring for GraphQL.
   * This method registers custom scalars, such as the JSON scalar, to extend the
   * GraphQL schema capabilities.
   *
   * @return the RuntimeWiringConfigurer bean used to customize the GraphQL runtime wiring
   */
  @Bean
  RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return wiringBuilder -> wiringBuilder.scalar(ExtendedScalars.Json);
  }
}
