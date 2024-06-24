package com.camila.api.product.framework.adapter.input.rsocket;

import com.camila.api.product.domain.Product;
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

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * The type R socket test client.
 */
@Slf4j
public class RSocketTestClient {
  // ws://localhost:7000/product-dev/api/socket
  // wss://poc.jpje-kops.xyz:7443/product/api/rsocket
  private static final String API_RSOCKET = "wss://poc.jpje-kops.xyz:7443/product/api/rsocket";

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    var rSocketStrategies = getrSocketStrategies();
    var requester = getrSocketRequester(rSocketStrategies);

    getProductsSortByWeights(requester);
    getProductByInternalId(requester);
  }

  private static RSocketStrategies getrSocketStrategies() {
    var objectMapper = new ObjectMapper(new CBORFactory());
    return RSocketStrategies.builder()
      .encoders(encoders -> encoders.add(new Jackson2CborEncoder(objectMapper)))
      .decoders(decoders -> decoders.add(new Jackson2CborDecoder(objectMapper)))
      .routeMatcher(new PathPatternRouteMatcher())
      .build();
  }

  private static RSocketRequester getrSocketRequester(RSocketStrategies rSocketStrategies) {
    return RSocketRequester.builder()
      .rsocketStrategies(rSocketStrategies)
      .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
      .websocket(URI.create(API_RSOCKET));
  }

  private static void getProductsSortByWeights(RSocketRequester requester) {
    logWithElapsedTime(requester, rSocketRequester -> {
      var future = new CompletableFuture<>();
      var message = """
      {
        "salesUnits": "0.9",
        "stock": "0.1",
        "page": "0",
        "size": "10000"
      }
      """;

      log.info("Started (getProductsSortByWeights)");
      requester.route("products.request-stream-sortByMetricsWeights")
        .data(message)
        .retrieveFlux(Product.class)
        .take(2000) // no matter if it's 2 or 100, it will only take 2
        .doOnError(throwable -> {
          log.error("Error (sortByMetricsWeights): {}", throwable.getMessage());
          future.completeExceptionally(throwable);
        })
        .doOnNext(product -> log.info("{} - {}", product.internalId(), product.name()))
        .doOnTerminate(() -> future.complete(null))
        .subscribe();

      try {
        future.join();
      } catch (Exception e) {
        log.error("Main thread interrupted", e);
      }
    });
  }

  private static void getProductByInternalId(RSocketRequester requester) {
    logWithElapsedTime(requester, rSocketRequester -> {
      var future = new CompletableFuture<>();
      var message = """
      {
        "internalId": "63132"
      }
      """;

      log.info("Started (getProductByInternalId)");
      requester.route("products.request-response-findByInternalId")
        .data(message)
        .retrieveMono(new ParameterizedTypeReference<Product>() {})
        .doOnError(throwable -> {
          log.error("Error (findByInternalId): {}", throwable.getMessage());
          future.completeExceptionally(throwable);
        })
        .doOnNext(product -> log.info("{}", product))
        .doOnTerminate(() -> future.complete(null))
        .subscribe();

      try {
        future.join();
      } catch (Exception e) {
        log.error("Main thread interrupted", e);
      }
    });
  }

  private static void logWithElapsedTime(RSocketRequester requester,
                                         Consumer<RSocketRequester> supplier) {
    var start = Instant.now();
    supplier.accept(requester);
    var end = Instant.now();
    var duration = Duration.between(start, end);
    log.info("Completed (getProductByInternalId) - time: {} ms", duration.toMillis());
  }
}
