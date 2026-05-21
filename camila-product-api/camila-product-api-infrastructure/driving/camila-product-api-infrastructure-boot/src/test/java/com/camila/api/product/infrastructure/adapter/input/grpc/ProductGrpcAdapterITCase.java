package com.camila.api.product.infrastructure.adapter.input.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Iterator;
import java.util.Map;

import com.camila.api.product.infrastructure.adapter.output.mongo.MongoContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.grpc.test.autoconfigure.LocalGrpcPort;
import org.springframework.grpc.client.GrpcChannelFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
  "spring.grpc.server.port=0",
  "spring.grpc.client.default-channel.negotiation-type=plaintext",
  "repository.technology=mongo"
})
@DisplayName("[IT][ProductGrpcAdapter] Product grpc adapter test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductGrpcAdapterITCase extends MongoContainerConfig {

  @LocalGrpcPort
  private int grpcPort;

  @Autowired
  private GrpcChannelFactory channelFactory;

  private ProductServiceGrpc.ProductServiceBlockingStub blockingStub;

  @BeforeEach
  void setUp() {
    this.blockingStub = ProductServiceGrpc.newBlockingStub(
      channelFactory.createChannel("0.0.0.0:" + grpcPort));
    assertNotNull(this.blockingStub);
  }

  @Test
  @DisplayName("[ProductGrpcAdapter] findByInternalId ok")
  @Order(5)
  void getProductByInternalIdOk() {
    // Given
    final var request = ProductInternalId.newBuilder().setInternalId("1").build();
    // When
    final var product = this.blockingStub.getProductByInternalId(request);
    // Then
    assertThat(product).isNotNull();
    assertThat(product.getInternalId()).isEqualTo("1");
  }

  @Test
  @DisplayName("[ProductGrpcAdapter] findByInternalId ko")
  @Order(5)
  void getProductByInternalIdKo() {
    // Given
    final var request = ProductInternalId.newBuilder().setInternalId("100").build();
    // When, Then
    assertThatException().isThrownBy(() -> this.blockingStub.getProductByInternalId(request));
  }

  @Test
  @DisplayName("[ProductGrpcAdapter] sort products with stock more weight ok")
  @Order(5)
  void sortByMetricsWeightsOk() {
    // Given
    final var request = SortByMetricsWeightsRequest.newBuilder()
      .putAllRequestParams(Map.of(
        "salesUnits", "0.0008",
        "stock", "0.9990",
        "profitMargin", "0.0001",
        "daysInStock", "0.0001"))
      .build();
    // When
    final Iterator<Product> productIterator = this.blockingStub.sortByMetricsWeights(request);
    // Then
    assertThat(productIterator.hasNext()).isTrue();
    productIterator.forEachRemaining(product -> {
      assertThat(product).isNotNull();
      assertThat(product.getInternalId()).isNotEqualTo("");
    });
  }
}
