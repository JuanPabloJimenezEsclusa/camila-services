package com.camila.api.product.domain.usecase;

import java.util.Map;

import com.camila.api.product.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The interface Product user case.
 */
public interface ProductUseCase {
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
   * @param requestParams the request params
   * @return the product flux
   */
  Flux<Product> sortByMetricsWeights(Map<String, String> requestParams);
}
