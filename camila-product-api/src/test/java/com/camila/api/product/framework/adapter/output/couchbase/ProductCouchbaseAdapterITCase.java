package com.camila.api.product.framework.adapter.output.couchbase;

import com.camila.api.product.application.port.output.ProductOutputPort;
import com.camila.api.product.domain.MetricWeight;
import com.camila.api.product.domain.Metrics;
import com.camila.api.product.domain.Product;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.couchbase.DataCouchbaseTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The type Product couchbase adapter it case.
 */

@Slf4j
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.camila.api.product.framework.adapter.output.couchbase")
@DataCouchbaseTest
@DisplayName("[IT][ProductCouchbaseAdapter] Product couchbase adapter test")
class ProductCouchbaseAdapterITCase extends AbstractProductCouchbaseAdapter {

  @Autowired
  @Qualifier(value = "productCouchbase")
  private ProductOutputPort productOutputPort;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    Assertions.assertNotNull(productOutputPort);
  }

  /**
   * Find by internal id ok.
   */
  @Test
  @DisplayName("[ProductCouchbaseAdapter] findByInternalId ok")
  void findByInternalIdOk() {
    Mono<Product> productMono = productOutputPort
      .findByInternalId("5")
      .doOnNext(product -> log.info(product.toString()));
    StepVerifier
      .create(productMono)
      .consumeNextWith(product -> {
        assertNotNull(product);
        assertEquals("CONTRASTING LACE T-SHIRT", product.name());
        assertEquals("SHIRT", product.category());
        assertEquals(650, product.salesUnits());
      })
      .verifyComplete();
  }

  /**
   * Sort by metrics stock weights more.
   */
  @Test
  @DisplayName("[ProductCouchbaseAdapter] sort with more stock weight")
  void sortByMetricsStockWeightsMore() {
    List<MetricWeight> metricsWeights = List.of(
      new MetricWeight(Metrics.SALES_UNITS, 0.001),
      new MetricWeight(Metrics.STOCK, 0.999)
    );
    PageRequest pageRequest = PageRequest.of(0, 100);
    Flux<Product> productFlux = productOutputPort.sortByMetricsWeights(metricsWeights, pageRequest);
    StepVerifier
      .create(productFlux)
      .consumeNextWith(product -> {
        assertNotNull(product.id());
        assertEquals("PLEATED T-SHIRT", product.name());
        assertEquals("SHIRT", product.category());
        assertEquals(3, product.salesUnits());
        assertEquals(Map.of("S", 25, "M", 30, "L", 10), product.stock());
      })
      .consumeNextWith(product -> {
        assertNotNull(product.id());
        assertEquals("CONTRASTING FABRIC T-SHIRT", product.name());
        assertEquals("SHIRT", product.category());
        assertEquals(50, product.salesUnits());
        assertEquals(Map.of("S", 35, "M", 9, "L", 9), product.stock());
      })
      .expectNextCount(4)
      .verifyComplete();
  }

  /**
   * Sort by metrics sales units weights more.
   */
  @Test
  @DisplayName("[ProductCouchbaseAdapter] sort with more sales units weight")
  void sortByMetricsSalesUnitsWeightsMore() {
    List<MetricWeight> metricsWeights = List.of(
      new MetricWeight(Metrics.SALES_UNITS, 0.9),
      new MetricWeight(Metrics.STOCK, 0.1)
    );
    PageRequest pageRequest = PageRequest.of(0, 10);
    Flux<Product> productFlux = productOutputPort.sortByMetricsWeights(metricsWeights, pageRequest);
    StepVerifier
      .create(productFlux)
      .consumeNextWith(product -> {
        assertNotNull(product.id());
        assertEquals("CONTRASTING LACE T-SHIRT", product.name());
        assertEquals("SHIRT", product.category());
        assertEquals(650, product.salesUnits());
        assertEquals(Map.of("S", 0, "M", 1, "L", 0), product.stock());
      })
      .consumeNextWith(product -> {
        assertNotNull(product.id());
        assertEquals("V-NECH BASIC SHIRT", product.name());
        assertEquals("SHIRT", product.category());
        assertEquals(100, product.salesUnits());
        assertEquals(Map.of("S", 4, "M", 9, "L", 0), product.stock());
      })
      .expectNextCount(4)
      .verifyComplete();
  }
}