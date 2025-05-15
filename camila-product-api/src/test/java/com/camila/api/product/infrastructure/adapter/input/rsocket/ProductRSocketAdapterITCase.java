package com.camila.api.product.infrastructure.adapter.input.rsocket;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URISyntaxException;

import com.camila.api.product.domain.model.Product;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.test.StepVerifier;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = {"repository.technology=mongo"}
)
@DisplayName("[IT][ProductRSocketAdapter] Product rsocket adapter test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductRSocketAdapterITCase {

  @Nullable
  private static RSocketRequester requester;

  @BeforeAll
  static void setUpOnce(@Autowired final RSocketRequester.Builder builder,
                        @LocalServerPort final Integer port) throws URISyntaxException {
    requester = builder.websocket(new URI("ws://localhost:%s/product-dev/api/rsocket".formatted(port)));
  }

  @Test
  @DisplayName("[ProductRSocketAdapter] find by internal id - ok")
  @Order(4)
  void findByInternalIdOk() {
    var message = """
      {
        "internalId": "1"
      }
      """;

    assert requester != null;
    requester.route("products.request-response-findByInternalId").data(message).retrieveMono(Product.class)
      .as(StepVerifier::create)
      .consumeNextWith(product -> {
        assertThat(product.internalId()).isEqualTo("1");
        assertThat(product.category()).isEqualTo("SHIRT");
        assertThat(product.name()).isEqualTo("V-NECH BASIC SHIRT");
      })
      .expectNextCount(0)
      .verifyComplete();
  }

  @Test
  @DisplayName("[ProductRSocketAdapter] find by internal id - ko")
  @Order(4)
  void findByInternalIdKo() {
    var message = """
      { }
      """;

    assert requester != null;
    requester.route("products.request-response-findByInternalId").data(message).retrieveMono(Product.class)
      .as(StepVerifier::create)
      .expectError()
      .verify();
  }

  @Test
  @DisplayName("[ProductRSocketAdapter] sort by metrics - sales units more weight - ok")
  @Order(4)
  void sortByMetricsSalesUnitsMoreWeightOk() {
    var message = """
      {
        "salesUnits": "0.9990",
        "stock": "0.0008",
        "profitMargin": "0.0001",
        "daysInStock": "0.0001",
        "page": "0",
        "size": "10"
      }
      """;

    assert requester != null;
    requester.route("products.request-stream-sortByMetricsWeights").data(message).retrieveFlux(Product.class)
      .as(StepVerifier::create)
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

  @Test
  @DisplayName("[ProductRSocketAdapter] sort by metrics - ko")
  @Order(4)
  void sortByMetricsKo() {
    var message = """
      { }
      """;

    assert requester != null;
    requester.route("products.request-stream-sortByMetricsWeights").data(message).retrieveFlux(Product.class)
      .as(StepVerifier::create)
      .expectError()
      .verify();
  }
}
