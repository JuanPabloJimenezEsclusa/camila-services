package com.camila.api.product.framework.adapter.input.rsocket;

import com.camila.api.product.domain.Product;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The type Product r socket adapter it case.
 */
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("[IT][ProductRSocketAdapter] Product rsocket adapter test")
class ProductRSocketAdapterITCase {

  @Nullable
  private static RSocketRequester requester;

  /**
   * Sets up once.
   *
   * @param builder the builder
   * @param port    the port
   * @throws URISyntaxException the uri syntax exception
   */
  @BeforeAll
  public static void setUpOnce(@Autowired RSocketRequester.Builder builder,
                               @LocalServerPort Integer port) throws URISyntaxException {
    requester = builder.websocket(new URI("ws://localhost:" + port + "/product-dev/api/rsocket"));
  }

  /**
   * Find by internal id ok.
   */
  @Test
  @DisplayName("[ProductRSocketAdapter] find by internal id - ok")
  void findByInternalIdOk() {
    var message = """
      {
        "internalId": "1"
      }
      """;

    assert requester != null;
    Mono<Product> productMono = requester
      .route("products.request-response-findByInternalId").data(message)
      .retrieveMono(Product.class);

    StepVerifier.create(productMono)
      .consumeNextWith(product -> {
        assertThat(product.internalId()).isEqualTo("1");
        assertThat(product.category()).isEqualTo("SHIRT");
        assertThat(product.name()).isEqualTo("V-NECH BASIC SHIRT");
      })
      .expectNextCount(0)
      .verifyComplete();
  }

  /**
   * Find by internal id ko.
   */
  @Test
  @DisplayName("[ProductRSocketAdapter] find by internal id - ko")
  void findByInternalIdKo() {
    var message = """
      { }
      """;

    assert requester != null;
    Mono<Product> productMono = requester
      .route("products.request-response-findByInternalId").data(message)
      .retrieveMono(Product.class);

    StepVerifier.create(productMono)
      .expectError()
      .verify();
  }

  /**
   * Sort by metrics sales units more weight ok.
   */
  @Test
  @DisplayName("[ProductRSocketAdapter] sort by metrics - sales units more weight - ok")
  void sortByMetricsSalesUnitsMoreWeightOk() {
    var message = """
      {
        "salesUnits": "0.9",
        "stock": "0.1",
        "page": "0",
        "size": "10"
      }
      """;

    assert requester != null;
    Flux<Product> productFlux = requester
      .route("products.request-stream-sortByMetricsWeights").data(message)
      .retrieveFlux(Product.class);

    StepVerifier.create(productFlux)
      .consumeNextWith(product -> {
        assertThat(product.internalId()).isEqualTo("5");
        assertThat(product.category()).isEqualTo("SHIRT");
        assertThat(product.name()).isEqualTo("CONTRASTING LACE T-SHIRT");
      })
      .consumeNextWith(product -> {
        assertThat(product.internalId()).isEqualTo("1");
        assertThat(product.category()).isEqualTo("SHIRT");
        assertThat(product.name()).isEqualTo("V-NECH BASIC SHIRT");
      })
      .expectNextCount(4)
      .verifyComplete();
  }

  /**
   * Sort by metrics ko.
   */
  @Test
  @DisplayName("[ProductRSocketAdapter] sort by metrics - ko")
  void sortByMetricsKo() {
    var message = """
      { }
      """;

    assert requester != null;
    Flux<Product> productFlux = requester
      .route("products.request-stream-sortByMetricsWeights").data(message)
      .retrieveFlux(Product.class);

    StepVerifier.create(productFlux)
      .expectError()
      .verify();
  }
}