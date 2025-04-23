package com.camila.api.product.infrastructure.adapter.output.mongo;

import java.util.Collections;
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
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

@EnableAutoConfiguration
@DataMongoTest
@ExtendWith(SpringExtension.class)
@EntityScan(basePackages = "com.camila.api.product.infrastructure.adapter.output.mongo")
@ComponentScan(basePackages = "com.camila.api.product.infrastructure.adapter.output.mongo")
@DisplayName("[IT][ProductMongoAdapter] Product mongo adapter test")
class ProductMongoAdapterITCase {

  @Autowired
  private ProductRepository productRepository;

  private static Stream<Arguments> sortScenarios() {
    return Stream.of(
      Arguments.of(
        "Stock-focused weights",
        List.of(new MetricWeight(Metrics.SALES_UNITS, 0.001), new MetricWeight(Metrics.STOCK, 0.999)),
        0, 100,
        "PLEATED T-SHIRT",
        "SHIRT",
        3,
        Map.of("S", 25, "M", 30, "L", 10)
      ),
      Arguments.of(
        "Sales-focused weights",
        List.of(new MetricWeight(Metrics.SALES_UNITS, 0.9), new MetricWeight(Metrics.STOCK, 0.1)),
        0, 10,
        "CONTRASTING LACE T-SHIRT",
        "SHIRT",
        650,
        Map.of("S", 0, "M", 1, "L", 0)
      ),
      Arguments.of(
        "Single metric - sales only",
        List.of(new MetricWeight(Metrics.SALES_UNITS, 1.0)),
        0, 10,
        "CONTRASTING LACE T-SHIRT",
        "SHIRT",
        650,
        Map.of("S", 0, "M", 1, "L", 0)
      ),
      Arguments.of(
        "Single metric - stock only",
        List.of(new MetricWeight(Metrics.STOCK, 1.0)),
        0, 10,
        "PLEATED T-SHIRT",
        "SHIRT",
        3,
        Map.of("S", 25, "M", 30, "L", 10)
      ),
      Arguments.of(
        "Unknown metric filtered out",
        List.of(new MetricWeight(Metrics.UNKNOWN, 1.0), new MetricWeight(Metrics.SALES_UNITS, 1.0)),
        0, 10,
        "CONTRASTING LACE T-SHIRT",
        "SHIRT",
        650,
        Map.of("S", 0, "M", 1, "L", 0)
      ),
      Arguments.of(
        "Default metrics with pagination",
        Collections.emptyList(),
        5, 5,
        "PLEATED T-SHIRT",
        "SHIRT",
        3,
        Map.of("S", 25, "M", 30, "L", 10)
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
          product.salesUnits() == 650)
      .verifyComplete();
  }

  @Test
  @DisplayName("Should return empty when product not found")
  void findByInternalIdNotFound() {
    // Given: A non-existent internal product ID
    final String nonExistentId = "999999";

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
                            final String expectedName, final String expectedCategory,
                            final int expectedSalesUnits, final Map<String, Integer> expectedStock) {
    // Given: Metric weights and page request
    // When: Sorting products by metric weights
    // Then: Should return products in correct order
    productRepository.sortByMetricsWeights(weights, offset, limit)
      .as(StepVerifier::create)
      .expectNextMatches(product ->
        product.name().equals(expectedName) &&
          product.category().equals(expectedCategory) &&
          product.salesUnits() == expectedSalesUnits &&
          product.stock().equals(expectedStock))
      .expectNextCount(scenario.equals("Default metrics with pagination") ? 0 : 1)
      .thenCancel()
      .verify();
  }
}