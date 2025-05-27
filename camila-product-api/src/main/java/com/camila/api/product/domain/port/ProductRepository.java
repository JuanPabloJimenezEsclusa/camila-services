package com.camila.api.product.domain.port;

import com.camila.api.product.domain.model.AppliedWeights;
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
   * @param appliedWeights the list of applied metric weights used for sorting
   * @param offset the starting point for the result set
   * @param limit the maximum number of products to return
   * @return a Flux emitting the sorted products
   */
  Flux<Product> sortByMetricsWeights(AppliedWeights appliedWeights, long offset, long limit);
}
