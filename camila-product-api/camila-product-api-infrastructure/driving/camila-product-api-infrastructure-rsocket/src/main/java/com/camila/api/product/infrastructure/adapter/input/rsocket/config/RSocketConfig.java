package com.camila.api.product.infrastructure.adapter.input.rsocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.cbor.JacksonCborDecoder;
import org.springframework.http.codec.cbor.JacksonCborEncoder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;
import tools.jackson.dataformat.cbor.CBORMapper;

@Configuration
public class RSocketConfig {

  @Bean
  RSocketMessageHandler messageHandler() {
    final var handler = new RSocketMessageHandler();
    handler.setRSocketStrategies(this.rsocketStrategies());
    return handler;
  }

  @Bean
  RSocketStrategies rsocketStrategies() {
    final var objectBuilderMapper = CBORMapper.builder();
    return RSocketStrategies.builder()
      .encoders(encoders -> encoders.add(new JacksonCborEncoder(objectBuilderMapper)))
      .decoders(decoders -> decoders.add(new JacksonCborDecoder(objectBuilderMapper)))
      .routeMatcher(new PathPatternRouteMatcher()).build();
  }
}
