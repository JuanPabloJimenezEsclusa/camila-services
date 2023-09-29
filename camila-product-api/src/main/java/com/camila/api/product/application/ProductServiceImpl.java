package com.camila.api.product.application;

import com.camila.api.product.domain.model.MetricWeight;
import com.camila.api.product.domain.model.Metrics;
import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.service.ProductService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The type Product service.
 */
@Service
class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;

  /**
   * Instantiates a new Product service.
   *
   * @param productRepository the product repository
   */
  public ProductServiceImpl(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public Mono<Product> findByInternalId(String internalId) {
    try {
      return productRepository.findByInternalId(internalId);
    } catch (Exception e){
      return Mono.error(new ProductServiceException(e));
    }
  }

  @Override
  public Flux<Product> sortByMetricsWeights(Map<String, String> requestParams) {
    try {
      return productRepository.sortByMetricsWeights(
        getMetricWeights(requestParams),
        getPageable(requestParams));
    } catch (Exception e) {
      return Flux.error(new ProductServiceException(e));
    }
  }

  private List<MetricWeight> getMetricWeights(Map<String, String> requestParams) {
    return requestParams.entrySet().stream()
      .filter(param -> Metrics.getMetrics(param.getKey()) != Metrics.UNKNOWN)
      .map(param -> new MetricWeight(Metrics.getMetrics(param.getKey()), Double.parseDouble(param.getValue())))
      .collect(Collectors.toList());
  }

  private Pageable getPageable(Map<String, String> requestParams) {
    int page = Integer.parseInt(Optional.ofNullable(requestParams.get("page")).orElse("0"));
    int size = Integer.parseInt(Optional.ofNullable(requestParams.get("size")).orElse("10"));
    return PageRequest.of(page, size);
  }
}

