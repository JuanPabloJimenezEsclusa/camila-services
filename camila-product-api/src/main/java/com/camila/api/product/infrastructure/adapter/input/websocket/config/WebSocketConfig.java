package com.camila.api.product.infrastructure.adapter.input.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import reactor.netty.http.server.WebsocketServerSpec;

import java.util.Map;

/**
 * The type Web socket config.
 */
@Configuration
@EnableWebFlux
public class WebSocketConfig {
  /**
   * Handler adapter web socket handler adapter.
   *
   * @return the web socket handler adapter
   */
  @Bean
  WebSocketHandlerAdapter handlerAdapter() {
    return new WebSocketHandlerAdapter(webSocketService());
  }

  /**
   * Web socket service web socket service.
   *
   * @return the web socket service
   */
  @Bean
  WebSocketService webSocketService() {
    return new HandshakeWebSocketService(
      new ReactorNettyRequestUpgradeStrategy(WebsocketServerSpec.builder()
        .maxFramePayloadLength(Integer.MAX_VALUE)));
  }

  /**
   * Web socket mapping simple url handler mapping.
   *
   * @param productWebSocketHandler the product web socket handler
   * @return the simple url handler mapping
   */
  @Bean
  SimpleUrlHandlerMapping webSocketMapping(final WebSocketHandler productWebSocketHandler) {
    return new SimpleUrlHandlerMapping(Map.of("/ws/products", productWebSocketHandler), -1);
  }
}
