package com.camila.api.product.domain.usecase;

import java.util.Map;

import com.camila.api.product.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The interface ProductUseCase.
 */
public interface ProductUseCase {

  /**
   * Finds a product by its internal ID.
   *
   * @param internalId the internal ID of the product
   * @return a Mono emitting the product if found, or empty if not found
   */
  Mono<Product> findByInternalId(String internalId);

  /**
   * Sorts products based on the provided request parameters.
   *
   * @param requestParams a map of request parameters used for sorting
   * @return a Flux emitting the sorted products
   */
  Flux<Product> sortByMetricsWeights(Map<String, String> requestParams);
}
