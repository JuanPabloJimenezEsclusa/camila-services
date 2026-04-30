package com.camila.api.product.infrastructure.adapter.input.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.net.URI;

import com.camila.api.product.infrastructure.adapter.output.mongo.MongoContainerConfig;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Mono;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"repository.technology=mongo"})
@DisplayName("[IT][ProductWebSocketHandler] Product websocket handler test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductWebSocketHandlerITCase extends MongoContainerConfig {

  @Nullable
  private static WebSocketClient webSocketClient;

  @Autowired
  private ObjectMapper objectMapper;

  @LocalServerPort
  private int randomPort;

  private URI uri;

  @BeforeAll
  static void beforeAll() {
    webSocketClient = new ReactorNettyWebSocketClient();
  }

  @BeforeEach
  void setUp() {
    assertNotNull(this.objectMapper);
    this.uri = URI.create("ws://localhost:%d/product-dev/api/ws/products".formatted(this.randomPort));
  }

  @Test
  @DisplayName("[ProductWebSocketHandler] handle find by internal id - Ok")
  @Order(3)
  void handleFindByInternalIdOk() {
    final String findByInternalIdRequest = """
      {
        "method": "FIND_BY_INTERNAL_ID",
        "internalId": "1"
      }
      """;

    assert webSocketClient != null;
    webSocketClient.execute(this.uri,
        session -> session.send(Mono.just(session.textMessage(findByInternalIdRequest)))
          .thenMany(session.receive().take(1).map(WebSocketMessage::getPayloadAsText))
          .flatMap(message -> {
            log.info("Received findByInternalI: {}", message);
            try {
              final var jsonNode = this.objectMapper.readTree(message);
              assertEquals(1, jsonNode.get("internalId").asInt());
              assertEquals("SHIRT", jsonNode.get("category").asString());
              assertEquals("V-NECH BASIC SHIRT", jsonNode.get("name").asString());
            } catch (final JacksonException e) {
              log.trace("Error parsing json", e);
            }
            return Mono.empty();
          }).then())
      .subscribe();
  }

  @Test
  @DisplayName("[ProductWebSocketHandler] handle sort products - Ok")
  @Order(3)
  void testSortProductsOk() {
    final String sortProductsRequest = """
      {
        "method": "SORT_PRODUCTS",
        "salesUnits": "0.80",
        "stock": "0.18",
        "profitMargin": "0.01",
        "daysInStock": "0.01",
        "page": "0",
        "size": "10"
      }
      """;

    assert webSocketClient != null;
    webSocketClient.execute(this.uri, session -> session.send(Mono.just(session.textMessage(sortProductsRequest)))
      .thenMany(session.receive().take(1).map(WebSocketMessage::getPayloadAsText)).flatMap(message -> {
        log.info("Received sortProducts: {}", message);
        try {
          final var jsonNode = this.objectMapper.readTree(message);
          assertEquals(5, jsonNode.get("internalId").asInt());
          assertEquals("SHIRT", jsonNode.get("category").asString());
          assertEquals("CONTRASTING LACE T-SHIRT", jsonNode.get("name").asString());
        } catch (final JacksonException e) {
          log.trace("Error parsing json", e);
        }
        return Mono.empty();
      }).then()).subscribe();
  }
}
