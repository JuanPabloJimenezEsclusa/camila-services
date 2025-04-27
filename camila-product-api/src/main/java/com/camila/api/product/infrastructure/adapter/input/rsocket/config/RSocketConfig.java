package com.camila.api.product.infrastructure.adapter.input.rsocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;

/**
 * The type RSocket config.
 */
@Configuration(proxyBeanMethods = false)
public class RSocketConfig {

  /**
   * Message handler rsocket message handler.
   *
   * @return the rsocket message handler
   */
  @Bean
  RSocketMessageHandler messageHandler() {
    final var handler = new RSocketMessageHandler();
    handler.setRSocketStrategies(rsocketStrategies());
    return handler;
  }

  /**
   * Rsocket strategies rsocket strategies.
   *
   * @return the rsocket strategies
   */
  @Bean
  RSocketStrategies rsocketStrategies() {
    // https://cbor.io/
    // https://www.rfc-editor.org/info/rfc7049
    final var objectMapper = new ObjectMapper(new CBORFactory());
    return RSocketStrategies.builder()
      .encoders(encoders -> encoders.add(new Jackson2CborEncoder(objectMapper)))
      .decoders(decoders -> decoders.add(new Jackson2CborDecoder(objectMapper)))
      .routeMatcher(new PathPatternRouteMatcher())
      .build();
  }
}
