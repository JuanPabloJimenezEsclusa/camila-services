package com.camila.api.product.infrastructure.adapter.input.rsocket;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.camila.api.product.domain.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;

/**
 * The type R socket test client.
 */
@Slf4j
public class RSocketTestClient {
  // ws://localhost:7000/product-dev/api/rsocket
  // ws://localhost:7000/product-int/api/rsocket
  // wss://poc.jpje-kops.xyz:7001/product/api/rsocket
  private static final String API_RSOCKET = "wss://poc.jpje-kops.xyz:7001/product/api/rsocket";

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    final var rsocketStrategies = getrSocketStrategies();
    final var requester = getrSocketRequester(rsocketStrategies);

    getProductsSortByWeights(requester);
    getProductByInternalId(requester);
  }

  private static RSocketStrategies getrSocketStrategies() {
    final var objectMapper = new ObjectMapper(new CBORFactory());
    return RSocketStrategies.builder()
      .encoders(encoders -> encoders.add(new Jackson2CborEncoder(objectMapper)))
      .decoders(decoders -> decoders.add(new Jackson2CborDecoder(objectMapper)))
      .routeMatcher(new PathPatternRouteMatcher())
      .build();
  }

  private static RSocketRequester getrSocketRequester(final RSocketStrategies rsocketStrategies) {
    return RSocketRequester.builder()
      .rsocketStrategies(rsocketStrategies)
      .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
      .websocket(URI.create(API_RSOCKET));
  }

  private static void getProductsSortByWeights(final RSocketRequester requester) {
    logWithElapsedTime(requester, rsocketRequester -> {
      final var future = new CompletableFuture<>();
      final var message = """
        {
          "salesUnits": "0.90",
          "stock": "0.18",
          "profitMargin": "0.01",
          "daysInStock": "0.01",
          "page": "0",
          "size": "10000"
        }
        """;

      log.info("Started (getProductsSortByWeights)");
      requester.route("products.request-stream-sortByMetricsWeights")
        .data(message)
        .retrieveFlux(Product.class)
        .take(2_000) // no matter if it's 2 or 100, it will only take 2
        .doOnError(throwable -> {
          log.debug("Error (sortByMetricsWeights): {}", throwable.getMessage());
          future.completeExceptionally(throwable);
        })
        .doOnNext(product -> log.info("{} - {}", product.internalId(), product.name()))
        .doOnTerminate(() -> future.complete(null))
        .subscribe();

      try {
        future.join();
      } catch (Exception e) {
        log.debug("Main thread interrupted", e);
      }
    });
  }

  private static void getProductByInternalId(final RSocketRequester requester) {
    logWithElapsedTime(requester, rsocketRequester -> {
      final var future = new CompletableFuture<>();
      final var message = """
        {
          "internalId": "63132"
        }
        """;

      log.info("Started (getProductByInternalId)");
      requester.route("products.request-response-findByInternalId")
        .data(message)
        .retrieveMono(new ParameterizedTypeReference<Product>() {
        })
        .doOnError(throwable -> {
          log.debug("Error (findByInternalId): {}", throwable.getMessage());
          future.completeExceptionally(throwable);
        })
        .doOnNext(product -> log.info("{}", product))
        .doOnTerminate(() -> future.complete(null))
        .subscribe();

      try {
        future.join();
      } catch (Exception e) {
        log.debug("Main thread interrupted", e);
      }
    });
  }

  private static void logWithElapsedTime(final RSocketRequester requester,
                                         final Consumer<RSocketRequester> supplier) {
    final var start = Instant.now();
    supplier.accept(requester);
    final var duration = Duration.between(start, Instant.now());
    log.info("Completed (getProductByInternalId) - time: {} ms", duration.toMillis());
  }
}
