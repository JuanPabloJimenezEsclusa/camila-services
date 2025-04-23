package com.camila.api.product.infrastructure.adapter.output.couchbase;

import java.util.List;

import com.camila.api.product.domain.model.MetricWeight;
import com.camila.api.product.domain.model.Metrics;
import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.port.ProductRepository;
import com.camila.api.product.infrastructure.adapter.output.couchbase.config.CouchbaseCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The type Product couchbase adapter.
 */
@Slf4j
@Conditional(CouchbaseCondition.class)
@Repository
public class ProductCouchbaseAdapter implements ProductRepository {
  private static final double DEFAULT_WEIGHT = 0.0000000001;
  private final ProductCouchbaseRepository productCouchbaseRepository;
  private final ProductCouchbaseMapper mapper;

  /**
   * Instantiates a new Product couchbase adapter.
   *
   * @param productCouchbaseRepository the product couchbase repository
   * @param mapper                     the mapper
   */
  public ProductCouchbaseAdapter(final ProductCouchbaseRepository productCouchbaseRepository,
                                 final ProductCouchbaseMapper mapper) {
    this.productCouchbaseRepository = productCouchbaseRepository;
    this.mapper = mapper;
  }

  @Override
  public Mono<Product> findByInternalId(final String internalId) {
    return productCouchbaseRepository.findByInternalId(internalId)
      .doOnNext(result -> log.debug("couchbase.adapter.findByInternalId: {}", result))
      .doOnError(throwable -> log.error("couchbase.adapter.findByInternalId: {}", throwable.getMessage()))
      .map(mapper::toProduct);
  }

  @Override
  public Flux<Product> sortByMetricsWeights(final List<MetricWeight> metricsWeights, final long offset, final long limit) {
    final double saleUnitsWeight = metricsWeights.stream()
      .filter(metricWeight -> metricWeight.metric() == Metrics.SALES_UNITS)
      .findFirst()
      .orElse(new MetricWeight(Metrics.SALES_UNITS, DEFAULT_WEIGHT)).weight();
    final double stockWeight = metricsWeights.stream()
      .filter(metricWeight -> metricWeight.metric() == Metrics.STOCK)
      .findFirst()
      .orElse(new MetricWeight(Metrics.STOCK, DEFAULT_WEIGHT)).weight();

    return productCouchbaseRepository.sortByMetricsWeights(saleUnitsWeight, stockWeight, limit, offset)
      .doOnNext(result -> log.debug("couchbase.adapter.sortByMetricsWeights: {}", result))
      .doOnError(throwable -> log.error("couchbase.adapter.sortByMetricsWeights: {}", throwable.getMessage()))
      .map(mapper::toProduct);
  }
}
