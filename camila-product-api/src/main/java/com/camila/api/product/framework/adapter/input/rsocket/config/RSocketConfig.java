package com.camila.api.product.framework.adapter.input.rsocket.config;

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
@Configuration
public class RSocketConfig {

  /**
   * Message handler rsocket message handler.
   *
   * @return the rsocket message handler
   */
  @Bean
  public RSocketMessageHandler messageHandler() {
    var handler = new RSocketMessageHandler();
    handler.setRSocketStrategies(rsocketStrategies());
    return handler;
  }

  /**
   * Rsocket strategies rsocket strategies.
   *
   * @return the rsocket strategies
   */
  @Bean
  public RSocketStrategies rsocketStrategies() {
    return RSocketStrategies.builder()
      .encoders(encoders -> encoders.add(new Jackson2CborEncoder()))
      .decoders(decoders -> decoders.add(new Jackson2CborDecoder()))
      .routeMatcher(new PathPatternRouteMatcher())
      .build();
  }
}
