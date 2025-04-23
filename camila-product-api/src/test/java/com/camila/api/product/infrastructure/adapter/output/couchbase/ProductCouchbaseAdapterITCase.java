package com.camila.api.product.infrastructure.adapter.output.couchbase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.camila.api.product.domain.model.MetricWeight;
import com.camila.api.product.domain.model.Metrics;
import com.camila.api.product.domain.port.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.couchbase.DataCouchbaseTest;
import org.springframework.context.annotation.ComponentScan;
import reactor.test.StepVerifier;

@EnableAutoConfiguration
@ExtendWith(ProductCouchbaseContainerConfig.class)
@ComponentScan(basePackages = "com.camila.api.product.infrastructure.adapter.output.couchbase")
@DataCouchbaseTest(properties = {
  "repository.technology=couchbase"
})
@DisplayName("[IT][ProductCouchbaseAdapter] Product Couchbase Adapter Integration Test")
class ProductCouchbaseAdapterITCase {

  @Autowired
  private ProductRepository productRepository;

  private static Stream<Arguments> sortScenarios() {
    return Stream.of(
      Arguments.of(
        "Stock-focused weights",
        List.of(new MetricWeight(Metrics.SALES_UNITS, 0.001), new MetricWeight(Metrics.STOCK, 0.999)),
        0L, 100L,
        "PLEATED T-SHIRT", "SHIRT", 3, Map.of("S", 25, "M", 30, "L", 10),
        "CONTRASTING FABRIC T-SHIRT", "SHIRT", 50, Map.of("S", 35, "M", 9, "L", 9),
        4
      ),
      Arguments.of(
        "Sales-focused weights",
        List.of(new MetricWeight(Metrics.SALES_UNITS, 0.9), new MetricWeight(Metrics.STOCK, 0.1)),
        0L, 10L,
        "CONTRASTING LACE T-SHIRT", "SHIRT", 650, Map.of("S", 0, "M", 1, "L", 0),
        "V-NECH BASIC SHIRT", "SHIRT", 100, Map.of("S", 4, "M", 9, "L", 0),
        4
      ),
      Arguments.of(
        "Single metric - sales only",
        List.of(new MetricWeight(Metrics.SALES_UNITS, 1.0)),
        0L, 5L,
        "CONTRASTING LACE T-SHIRT", "SHIRT", 650, Map.of("S", 0, "M", 1, "L", 0),
        "V-NECH BASIC SHIRT", "SHIRT", 100, Map.of("S", 4, "M", 9, "L", 0),
        3
      ),
      Arguments.of(
        "Single metric - stock only",
        List.of(new MetricWeight(Metrics.STOCK, 1.0)),
        0L, 5L,
        "PLEATED T-SHIRT", "SHIRT", 3, Map.of("S", 25, "M", 30, "L", 10),
        "CONTRASTING FABRIC T-SHIRT", "SHIRT", 50, Map.of("S", 35, "M", 9, "L", 9),
        3
      ),
      Arguments.of(
        "Pagination with offset",
        List.of(new MetricWeight(Metrics.SALES_UNITS, 0.5), new MetricWeight(Metrics.STOCK, 0.5)),
        1L, 2L,
        "V-NECH BASIC SHIRT", "SHIRT", 100, Map.of("S", 4, "M", 9, "L", 0),
        "RAISED PRINT T-SHIRT", "SHIRT", 80, Map.of("S", 20, "M", 2, "L", 20),
        0
      )
    );
  }

  @Test
  @DisplayName("Should find product by internal ID")
  void findByInternalId() {
    // Given: A valid internal product ID
    final String internalId = "5";

    // When: Finding a product by internal ID
    // Then: Should return the correct product
    productRepository.findByInternalId(internalId)
      .as(StepVerifier::create)
      .expectNextMatches(product ->
        product.name().equals("CONTRASTING LACE T-SHIRT") &&
          product.category().equals("SHIRT") &&
          product.salesUnits() == 650 &&
          product.stock().equals(Map.of("S", 0, "M", 1, "L", 0)))
      .verifyComplete();
  }

  @Test
  @DisplayName("Should return empty when product not found")
  void findByInternalIdNotFound() {
    // Given: A non-existent internal product ID
    final String nonExistentId = "999";

    // When: Finding a product by internal ID
    // Then: Should return empty
    productRepository.findByInternalId(nonExistentId)
      .as(StepVerifier::create)
      .verifyComplete();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("sortScenarios")
  @DisplayName("Should sort products by metric weights")
  void sortByMetricsWeights(final String scenario, final List<MetricWeight> weights,
                            final long offset, final long limit,
                            final String expectedFirstProductName, final String expectedFirstProductCategory,
                            final int expectedFirstProductSalesUnits, final Map<String, Integer> expectedFirstProductStock,
                            final String expectedSecondProductName, final String expectedSecondProductCategory,
                            final int expectedSecondProductSalesUnits, final Map<String, Integer> expectedSecondProductStock,
                            final int expectedRemainingCount) {
    // Given: Metric weights and pagination parameters
    // When: Sorting products by metric weights
    // Then: Should return products in correct order
    productRepository.sortByMetricsWeights(weights, offset, limit)
      .as(StepVerifier::create)
      .expectNextMatches(product -> {
        assertThat(product.id()).isNotNull();
        assertThat(product.name()).isEqualTo(expectedFirstProductName);
        assertThat(product.category()).isEqualTo(expectedFirstProductCategory);
        assertThat(product.salesUnits()).isEqualTo(expectedFirstProductSalesUnits);
        assertThat(product.stock()).isEqualTo(expectedFirstProductStock);
        return true;
      })
      .expectNextMatches(product -> {
        assertThat(product.id()).isNotNull();
        assertThat(product.name()).isEqualTo(expectedSecondProductName);
        assertThat(product.category()).isEqualTo(expectedSecondProductCategory);
        assertThat(product.salesUnits()).isEqualTo(expectedSecondProductSalesUnits);
        assertThat(product.stock()).isEqualTo(expectedSecondProductStock);
        return true;
      })
      .expectNextCount(expectedRemainingCount)
      .verifyComplete();
  }
}