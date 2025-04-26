package com.camila.api.product.domain.port;

import java.util.List;

import com.camila.api.product.domain.model.MetricWeight;
import com.camila.api.product.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The interface Product repository.
 */
public interface ProductRepository {
  /**
   * Find by internal id.
   *
   * @param internalId the internal id
   * @return the product
   */
  Mono<Product> findByInternalId(String internalId);

  /**
   * Sort by metrics weights.
   *
   * @param metricsWeights the metrics weights
   * @param offset the offset
   * @param limit the limit
   * @return the product flux
   */
  Flux<Product> sortByMetricsWeights(List<MetricWeight> metricsWeights, long offset, long limit);
}
