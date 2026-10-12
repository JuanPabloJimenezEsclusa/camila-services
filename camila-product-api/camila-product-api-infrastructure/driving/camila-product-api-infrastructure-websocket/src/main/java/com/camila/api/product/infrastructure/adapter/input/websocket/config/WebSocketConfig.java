package com.camila.api.product.infrastructure.adapter.input.websocket.config;

import java.util.Map;

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

@Configuration
@EnableWebFlux
public class WebSocketConfig {

  @Bean
  WebSocketHandlerAdapter handlerAdapter() {
    return new WebSocketHandlerAdapter(this.webSocketService());
  }

  @Bean
  WebSocketService webSocketService() {
    return new HandshakeWebSocketService(new ReactorNettyRequestUpgradeStrategy(
      WebsocketServerSpec.builder().maxFramePayloadLength(Integer.MAX_VALUE)));
  }

  @Bean
  SimpleUrlHandlerMapping webSocketMapping(final WebSocketHandler productWebSocketHandler) {
    return new SimpleUrlHandlerMapping(Map.of("/ws/products", productWebSocketHandler), -1);
  }
}
