package com.camila.api.product.infrastructure.adapter.input.rsocket;

import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.usecase.ProductUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * The type Product rsocket adapter.
 */
@Slf4j
@Controller
@MessageMapping("products")
class ProductRSocketAdapter {
  private static final String INTERNAL_ID = "internalId";
  private static final String SALES_UNITS = "salesUnits";
  private static final String STOCK = "stock";
  private static final String PAGE = "page";
  private static final String SIZE = "size";

  private final ProductUseCase productUseCase;
  private final ObjectMapper objectMapper;

  /**
   * Instantiates a new Product rsocket adapter.
   *
   * @param productUseCase the product user case
   * @param objectMapper   the object mapper
   */
  ProductRSocketAdapter(final ProductUseCase productUseCase, final ObjectMapper objectMapper) {
    this.productUseCase = productUseCase;
    this.objectMapper = objectMapper;
  }

  /**
   * Find by internal id mono.
   *
   * @param message the message
   * @return the mono
   * @throws JsonProcessingException the json processing exception
   */
  @MessageMapping("request-response-findByInternalId")
  public Mono<Product> findByInternalId(final String message) throws JsonProcessingException {
    return validateAndBuildFindRequest(message).flatMap(internalId ->
      productUseCase.findByInternalId(internalId)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Product not found")))
        .doOnNext(product -> log.info("findByInternalId.next: {}", product))
        .doOnError(throwable -> log.error("findByInternalId.error: {}", throwable.getMessage())));
  }

  /**
   * Sort by metrics weights flux.
   *
   * @param message the message
   * @return the flux
   * @throws JsonProcessingException the json processing exception
   */
  @MessageMapping("request-stream-sortByMetricsWeights")
  public Flux<Product> sortByMetricsWeights(final String message) throws JsonProcessingException {
    return validateAndBuildSortRequest(message)
      .flatMap(requestParams -> productUseCase.sortByMetricsWeights(requestParams)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Product collection empty")))
        .doOnNext(product -> log.info("sortByMetricsWeights.next: {}", product))
        .doOnError(throwable -> log.error("sortByMetricsWeights.error: {}", throwable.getMessage())));
  }

  private static JsonNode validate(final JsonNode jsonNode, final String fieldName) {
    if (!jsonNode.hasNonNull(fieldName)) {
      throw new IllegalArgumentException("%s is required".formatted(fieldName));
    }
    return jsonNode;
  }

  private Mono<String> validateAndBuildFindRequest(final String message) {
    try {
      final var jsonNode = objectMapper.readTree(message);
      return Mono.just(validate(jsonNode, INTERNAL_ID).get(INTERNAL_ID).asText("0"));
    } catch (final Exception e) {
      return Mono.error(e);
    }
  }

  private Flux<Map<String, String>> validateAndBuildSortRequest(final String message) {
    try {
      final var jsonNode = objectMapper.readTree(message);
      return Flux.just(Map.of(
        SALES_UNITS, validate(jsonNode, SALES_UNITS).get(SALES_UNITS).asText("0.001"),
        STOCK, validate(jsonNode, STOCK).get(STOCK).asText("0.999"),
        PAGE, validate(jsonNode, PAGE).get(PAGE).asText("0"),
        SIZE, validate(jsonNode, SIZE).get(SIZE).asText("25")));
    } catch (final Exception e) {
      return Flux.error(e);
    }
  }
}
