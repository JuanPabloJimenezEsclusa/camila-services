package com.camila.api.product.infrastructure.adapter.input.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Iterator;
import java.util.Map;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = {
    "grpc.server.port=6565",
    "grpc.client.product-service.address=static://localhost:6565",
    "grpc.client.product-service.negotiation-type=plaintext",
    "repository.technology=mongo"
  }
)
@DisplayName("[IT][ProductGrpcAdapter] Product grpc adapter test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductGrpcAdapterITCase {
  @GrpcClient("product-service")
  private ProductServiceGrpc.ProductServiceBlockingStub blockingStub;

  @BeforeEach
  void setUp() {
    assertNotNull(blockingStub);
  }

  @Test
  @DisplayName("[ProductGrpcAdapter] findByInternalId ok")
  @Order(5)
  void getProductByInternalIdOk() {
    // Given
    final var request = ProductInternalId.newBuilder()
      .setInternalId("1")
      .build();
    // When
    final var product = blockingStub.getProductByInternalId(request);
    // Then
    assertThat(product).isNotNull();
    assertThat(product.getInternalId()).isEqualTo("1");
  }

  @Test
  @DisplayName("[ProductGrpcAdapter] findByInternalId ko")
  @Order(5)
  void getProductByInternalIdKo() {
    // Given
    final var request = ProductInternalId.newBuilder()
      .setInternalId("100")
      .build();
    // When, Then
    assertThatException().isThrownBy(() -> blockingStub.getProductByInternalId(request));
  }

  @Test
  @DisplayName("[ProductGrpcAdapter] sort products with stock more weight ok")
  @Order(5)
  void sortByMetricsWeightsOk() {
    // Given
    final var request = SortByMetricsWeightsRequest.newBuilder()
      .putAllRequestParams(Map.of("salesUnits", "0.001", "stock", "0.999"))
      .build();
    // When
    final Iterator<Product> productIterator = blockingStub.sortByMetricsWeights(request);
    // Then
    assertThat(productIterator.hasNext()).isTrue();
    productIterator.forEachRemaining(product -> {
      assertThat(product).isNotNull();
      assertThat(product.getInternalId()).isNotEqualTo("");
    });
  }
}
