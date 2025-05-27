package com.camila.api.product.infrastructure.adapter.input.websocket;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.usecase.ProductUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The type Product web socket handler.
 */
@Component
public class ProductWebSocketHandler implements WebSocketHandler {

  private static final String DEFAULT_WEIGHT = "0.0000000001";
  private static final String DEFAULT_PAGE = "0";
  private static final String DEFAULT_SIZE = "25";

  private final ProductUseCase productUseCase;
  private final ObjectMapper objectMapper;

  /**
   * Instantiates a new Product web socket handler.
   *
   * @param productUseCase the product user case
   * @param objectMapper the object mapper
   */
  ProductWebSocketHandler(final ProductUseCase productUseCase, final ObjectMapper objectMapper) {
    this.productUseCase = productUseCase;
    this.objectMapper = objectMapper;
  }

  @Override
  public Mono<Void> handle(final WebSocketSession session) {
    final Flux<String> receive = session.receive()
      .map(webSocketMessage -> webSocketMessage.getPayloadAsText(StandardCharsets.UTF_8))
      .flatMap(this::handleMessage);

    return session.send(receive.map(session::textMessage));
  }

  private Flux<String> handleMessage(final String message) {
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

  private Flux<String> handleFindByInternalId(final JsonNode jsonNode) {
    return productUseCase
      .findByInternalId(jsonNode.get("internalId").asText())
      .flatMapMany(this::convertProductToString)
      .switchIfEmpty(Flux.just("Product not found"));
  }

  private Flux<String> handleSortProducts(final JsonNode jsonNode) {
    var requestParams = Map.of(
      "salesUnits", getNodeTextOrDefault(jsonNode, "salesUnits", DEFAULT_WEIGHT),
      "stock", getNodeTextOrDefault(jsonNode, "stock", DEFAULT_WEIGHT),
      "profitMargin", getNodeTextOrDefault(jsonNode, "profitMargin", DEFAULT_WEIGHT),
      "daysInStock", getNodeTextOrDefault(jsonNode, "daysInStock", DEFAULT_WEIGHT),
      "page", getNodeTextOrDefault(jsonNode, "page", DEFAULT_PAGE),
      "size", getNodeTextOrDefault(jsonNode, "size", DEFAULT_SIZE)
    );
    return productUseCase.sortByMetricsWeights(requestParams)
      .flatMap(this::convertProductToString)
      .switchIfEmpty(Flux.just("No products found"));
  }

  private Flux<String> convertProductToString(final Product product) {
    try {
      return Flux.just(objectMapper.writeValueAsString(product));
    } catch (JsonProcessingException e) {
      return Flux.just("Error converting product to string: " + e.getMessage());
    }
  }

  private String getNodeTextOrDefault(final JsonNode parentNode, final String fieldName, final String defaultValue) {
    return Optional.ofNullable(parentNode.get(fieldName)).map(JsonNode::asText).orElse(defaultValue);
  }

  enum SocketMethod {
    FIND_BY_INTERNAL_ID,
    SORT_PRODUCTS
  }
}
