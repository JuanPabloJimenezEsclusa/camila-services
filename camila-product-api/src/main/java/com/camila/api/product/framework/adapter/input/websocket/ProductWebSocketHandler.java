package com.camila.api.product.framework.adapter.input.websocket;

import com.camila.api.product.application.usercase.ProductUserCase;
import com.camila.api.product.domain.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * The type Product web socket handler.
 */
@Component
public class ProductWebSocketHandler implements WebSocketHandler {
  private final ProductUserCase productUserCase;
  private final ObjectMapper objectMapper;

  /**
   * Instantiates a new Product web socket handler.
   *
   * @param productUserCase the product user case
   * @param objectMapper    the object mapper
   */
  ProductWebSocketHandler(ProductUserCase productUserCase, ObjectMapper objectMapper) {
    this.productUserCase = productUserCase;
    this.objectMapper = objectMapper;
  }

  @Override
  public Mono<Void> handle(WebSocketSession session) {
    Flux<String> receive = session.receive()
      .map(WebSocketMessage::getPayloadAsText)
      .flatMap(this::handleMessage);

    return session.send(receive.map(session::textMessage));
  }

  private Flux<String> handleMessage(String message) {
    try {
      var jsonNode = objectMapper.readTree(message);
      var method = jsonNode.get("method").asText("1");
      return switch (SocketMethod.valueOf(method)) {
        case SocketMethod.FIND_BY_INTERNAL_ID -> handleFindByInternalId(jsonNode);
        case SocketMethod.SORT_PRODUCTS -> handleSortProducts(jsonNode);
      };
    } catch (JsonProcessingException e) {
      return Flux.just("Error converting request to Json:  " + e.getMessage());
    }
  }

  private Flux<String> handleFindByInternalId(JsonNode jsonNode) {
    return productUserCase
      .findByInternalId(jsonNode.get("internalId").asText())
      .flatMapMany(this::convertProductToString)
      .switchIfEmpty(Flux.just("Product not found"));
  }

  private Flux<String> handleSortProducts(JsonNode jsonNode) {
    var requestParams = Map.of(
      "salesUnits", jsonNode.get("salesUnits").asText("0.001"),
      "stock", jsonNode.get("stock").asText("0.999"),
      "page", jsonNode.get("page").asText("0"),
      "size", jsonNode.get("size").asText("25")
    );
    return productUserCase.sortByMetricsWeights(requestParams)
      .flatMap(this::convertProductToString)
      .switchIfEmpty(Flux.just("No products found"));
  }

  private Flux<String> convertProductToString(Product product) {
    try {
      return Flux.just(objectMapper.writeValueAsString(product));
    } catch (JsonProcessingException e) {
      return Flux.just("Error converting product to string: " + e.getMessage());
    }
  }
}

enum SocketMethod {
  FIND_BY_INTERNAL_ID,
  SORT_PRODUCTS;
}
