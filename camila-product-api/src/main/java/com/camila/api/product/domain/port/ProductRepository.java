package com.camila.api.product.domain.port;

import java.util.List;

import com.camila.api.product.domain.model.MetricWeight;
import com.camila.api.product.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The interface ProductRepository.
 */
public interface ProductRepository {

  /**
   * Finds a product by its internal ID.
   *
   * @param internalId the internal ID of the product
   * @return a Mono emitting the product if found, or empty if not found
   */
  Mono<Product> findByInternalId(String internalId);

  /**
   * Sorts products based on a list of metric weights.
   *
   * @param metricsWeights the list of metric weights used for sorting
   * @param offset the starting point for the result set
   * @param limit the maximum number of products to return
   * @return a Flux emitting the sorted products
   */
  Flux<Product> sortByMetricsWeights(List<MetricWeight> metricsWeights, long offset, long limit);
}
