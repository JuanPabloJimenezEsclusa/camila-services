package com.camila.api.product.application;

import com.camila.api.product.domain.model.MetricWeight;
import com.camila.api.product.domain.model.Product;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * The interface Product repository.
 */
public interface ProductRepository {
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
   * @param metricsWeights the metrics weights
   * @param paging         the paging
   * @return the flux
   */
  Flux<Product> sortByMetricsWeights(List<MetricWeight> metricsWeights, Pageable paging);
}
