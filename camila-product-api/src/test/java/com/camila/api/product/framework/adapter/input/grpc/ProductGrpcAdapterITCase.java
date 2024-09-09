package com.camila.api.product.framework.adapter.input.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The type Product grpc adapter it case.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("[IT][ProductGrpcAdapter] Product grpc adapter test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = "grpc.server.port=6565")
class ProductGrpcAdapterITCase {
  private static ManagedChannel channel;

  @GrpcClient("product-service")
  private ProductServiceGrpc.ProductServiceBlockingStub blockingStub;

  /**
   * Sets .
   */
  @BeforeAll
  public static void setup() {
    channel = ManagedChannelBuilder
      .forAddress("localhost", 6565)
      .usePlaintext()
      .build();
  }

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    assertNotNull(blockingStub);
  }

  /**
   * Tear down.
   */
  @AfterAll
  public static void tearDown() {
    channel.shutdown();
  }

  /**
   * Gets product by internal id ok.
   */
  @Test
  @DisplayName("[ProductGrpcAdapter] findByInternalId ok")
  @Order(5)
  void getProductByInternalIdOk() {
    // Given
    ProductInternalId request = ProductInternalId.newBuilder()
      .setInternalId("1")
      .build();
    // When
    Product product = blockingStub.getProductByInternalId(request);
    // Then
    assertThat(product).isNotNull();
    assertThat(product.getInternalId()).isEqualTo("1");
  }

  /**
   * Gets product by internal id ko.
   */
  @Test
  @DisplayName("[ProductGrpcAdapter] findByInternalId ko")
  @Order(5)
  void getProductByInternalIdKo() {
    // Given
    ProductInternalId request = ProductInternalId.newBuilder()
      .setInternalId("100")
      .build();
    // When
    // Then
    assertThatException().isThrownBy(() -> blockingStub.getProductByInternalId(request));
  }

  /**
   * Sort by metrics weights ok.
   */
  @Test
  @DisplayName("[ProductGrpcAdapter] sort products with stock more weight ok")
  @Order(5)
  void sortByMetricsWeightsOk() {
    // Given
    SortByMetricsWeightsRequest request = SortByMetricsWeightsRequest.newBuilder()
      .putAllRequestParams(Map.of("salesUnits", "0.001", "stock", "0.999"))
      .build();
    // When
    Iterator<Product> productIterator = blockingStub.sortByMetricsWeights(request);
    // Then
    assertThat(productIterator.hasNext()).isTrue();
    productIterator.forEachRemaining(product -> {
      assertThat(product).isNotNull();
      assertThat(product.getInternalId()).isNotEqualTo("");
    });
  }
}
