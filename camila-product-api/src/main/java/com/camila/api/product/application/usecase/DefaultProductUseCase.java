package com.camila.api.product.application.usecase;

import java.util.Map;

import com.camila.api.product.domain.exception.ProductException;
import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.model.ProductSortCriteria;
import com.camila.api.product.domain.port.ProductRepository;
import com.camila.api.product.domain.service.ProductWeightResolver;
import com.camila.api.product.domain.usecase.ProductUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The type Default product use case.
 */
public class DefaultProductUseCase implements ProductUseCase {

  private static final Logger log = LoggerFactory.getLogger(DefaultProductUseCase.class);

  private final ProductRepository productRepository;

  /**
   * Instantiates a new Default product use case.
   *
   * @param productRepository the product repository used for data access
   */
  public DefaultProductUseCase(final ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public Mono<Product> findByInternalId(final String internalId) {
    return productRepository.findByInternalId(internalId)
      .doOnNext(product -> log.debug("find By Id: {}", product))
      .onErrorResume(e -> Mono.error(new ProductException(e)));
  }

  @Override
  public Flux<Product> sortByMetricsWeights(final Map<String, String> requestParams) {
    try {
      final var criteria = ProductSortCriteria.fromRequestParams(requestParams);
      final var appliedWeights = ProductWeightResolver.resolve(criteria.getMetricWeights());
      return productRepository.sortByMetricsWeights(appliedWeights, criteria.getOffset(), criteria.getLimit())
        .doOnNext(product -> log.debug("find sort by: {}", product))
        .onErrorResume(e -> Flux.error(new ProductException(e)));
    } catch (IllegalArgumentException e) {
      return Flux.error(e);
    }
  }
}
