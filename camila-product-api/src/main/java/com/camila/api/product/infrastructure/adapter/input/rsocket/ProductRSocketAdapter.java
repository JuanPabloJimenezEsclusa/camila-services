package com.camila.api.product.infrastructure.adapter.input.rsocket;

import java.util.Map;

import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.usecase.ProductUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The type Product rsocket adapter.
 */
@Slf4j
@Controller
@MessageMapping("products")
class ProductRSocketAdapter {
  private static final String DEFAULT_WEIGHT = "0.0000000001";
  private static final String DEFAULT_PAGE = "0";
  private static final String DEFAULT_SIZE = "25";

  private static final String INTERNAL_ID = "internalId";
  private static final String SALES_UNITS = "salesUnits";
  private static final String STOCK = "stock";
  private static final String PROFIT_MARGIN = "profitMargin";
  private static final String DAYS_IN_STOCK = "daysInStock";
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

  private static JsonNode validate(final JsonNode jsonNode, final String fieldName) {
    if (!jsonNode.hasNonNull(fieldName)) {
      throw new IllegalArgumentException("%s is required".formatted(fieldName));
    }
    return jsonNode;
  }

  /**
   * Find by internal id.
   *
   * @param message the message
   * @return the product
   * @throws JsonProcessingException the json processing exception
   */
  @MessageMapping("request-response-findByInternalId")
  public Mono<Product> findByInternalId(final String message) throws JsonProcessingException {
    return validateAndBuildFindRequest(message).flatMap(internalId ->
      productUseCase.findByInternalId(internalId)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Product not found")))
        .doOnNext(product -> log.info("findByInternalId.next: {}", product))
        .doOnError(throwable -> log.debug("findByInternalId.error: {}", throwable.getMessage())));
  }

  /**
   * Sort by metrics weights flux.
   *
   * @param message the message
   * @return the product flux
   * @throws JsonProcessingException the json processing exception
   */
  @MessageMapping("request-stream-sortByMetricsWeights")
  public Flux<Product> sortByMetricsWeights(final String message) throws JsonProcessingException {
    return validateAndBuildSortRequest(message)
      .flatMap(requestParams -> productUseCase.sortByMetricsWeights(requestParams)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Product collection empty")))
        .doOnNext(product -> log.info("sortByMetricsWeights.next: {}", product))
        .doOnError(throwable -> log.debug("sortByMetricsWeights.error: {}", throwable.getMessage())));
  }

  /**
   * Handle exception.
   *
   * @param exception the exception
   * @return Void
   */
  @MessageExceptionHandler
  public Mono<Void> handleException(final Exception exception) {
    return Mono.error(exception);
  }

  private Mono<String> validateAndBuildFindRequest(final String message) {
    try {
      final var jsonNode = objectMapper.readTree(message);
      return Mono.just(validate(jsonNode, INTERNAL_ID).get(INTERNAL_ID).asText(DEFAULT_PAGE));
    } catch (final Exception e) {
      return Mono.error(e);
    }
  }

  private Flux<Map<String, String>> validateAndBuildSortRequest(final String message) {
    try {
      final var jsonNode = objectMapper.readTree(message);
      return Flux.just(Map.of(
        SALES_UNITS, validate(jsonNode, SALES_UNITS).get(SALES_UNITS).asText(DEFAULT_WEIGHT),
        STOCK, validate(jsonNode, STOCK).get(STOCK).asText(DEFAULT_WEIGHT),
        PROFIT_MARGIN, validate(jsonNode, PROFIT_MARGIN).get(PROFIT_MARGIN).asText(DEFAULT_WEIGHT),
        DAYS_IN_STOCK, validate(jsonNode, DAYS_IN_STOCK).get(DAYS_IN_STOCK).asText(DEFAULT_WEIGHT),
        PAGE, validate(jsonNode, PAGE).get(PAGE).asText(DEFAULT_PAGE),
        SIZE, validate(jsonNode, SIZE).get(SIZE).asText(DEFAULT_SIZE)));
    } catch (final Exception e) {
      return Flux.error(e);
    }
  }
}
