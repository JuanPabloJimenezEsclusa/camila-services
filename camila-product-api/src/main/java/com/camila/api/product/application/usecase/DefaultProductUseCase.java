package com.camila.api.product.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.camila.api.product.domain.exception.ProductException;
import com.camila.api.product.domain.model.MetricWeight;
import com.camila.api.product.domain.model.Metrics;
import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.port.ProductRepository;
import com.camila.api.product.domain.usecase.ProductUseCase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The type Default product use case.
 */
public class DefaultProductUseCase implements ProductUseCase {

  private static final String DEFAULT_PAGE_NUMBER = "0";
  private static final String DEFAULT_PAGE_SIZE = "10";

  private final ProductRepository productRepository;

  /**
   * Instantiates a new Default product use case.
   *
   * @param productRepository the product output port
   */
  public DefaultProductUseCase(final ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  private static List<MetricWeight> getMetricWeights(final Map<String, String> requestParams) {
    return requestParams.entrySet().stream()
      .filter(param -> Metrics.getMetrics(param.getKey()) != Metrics.UNKNOWN)
      .map(param -> new MetricWeight(Metrics.getMetrics(param.getKey()), Double.parseDouble(param.getValue())))
      .toList();
  }

  private static long getOffset(final Map<String, String> requestParams) {
    long page = Long.parseLong(Optional.ofNullable(requestParams.get("page")).orElse(DEFAULT_PAGE_NUMBER));
    long size = Long.parseLong(Optional.ofNullable(requestParams.get("size")).orElse(DEFAULT_PAGE_SIZE));
    return page * size;
  }

  private static long getLimit(final Map<String, String> requestParams) {
    return Long.parseLong(Optional.ofNullable(requestParams.get("size")).orElse(DEFAULT_PAGE_SIZE));
  }

  @Override
  public Mono<Product> findByInternalId(final String internalId) {
    try {
      return productRepository.findByInternalId(internalId);
    } catch (Exception e) {
      return Mono.error(new ProductException(e));
    }
  }

  @Override
  public Flux<Product> sortByMetricsWeights(final Map<String, String> requestParams) {
    try {
      return productRepository.sortByMetricsWeights(
        getMetricWeights(requestParams),
        getOffset(requestParams),
        getLimit(requestParams));
    } catch (Exception e) {
      return Flux.error(new ProductException(e));
    }
  }
}
