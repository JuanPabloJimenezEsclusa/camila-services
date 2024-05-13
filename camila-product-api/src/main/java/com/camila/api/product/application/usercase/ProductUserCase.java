package com.camila.api.product.application.usercase;

import com.camila.api.product.domain.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * The interface Product user case.
 */
public interface ProductUserCase {
  /**
   * Find by internal id mono.
   *
   * @param internalId the internal id
   * @return the mono
   */
  Mono<Product> findByInternalId(String internalId);

  /**
   * Sort by metrics weights flux.
   *
   * @param requestParams the request params
   * @return the flux
   */
  Flux<Product> sortByMetricsWeights(Map<String, String> requestParams);
}
