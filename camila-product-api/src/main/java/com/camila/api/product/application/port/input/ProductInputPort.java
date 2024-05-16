package com.camila.api.product.application.port.input;

import com.camila.api.product.application.exception.ProductException;
import com.camila.api.product.application.port.output.ProductOutputPort;
import com.camila.api.product.application.usercase.ProductUserCase;
import com.camila.api.product.domain.MetricWeight;
import com.camila.api.product.domain.Metrics;
import com.camila.api.product.domain.Product;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The type Product input port.
 */
@Service
class ProductInputPort implements ProductUserCase {

  private final ProductOutputPort productOutputPort;

  /**
   * Instantiates a new Product service.
   *
   * @param productOutputPort the product mongo
   */
  public ProductInputPort(@Qualifier(value = "productCouchbase") ProductOutputPort productOutputPort) {
    this.productOutputPort = productOutputPort;
  }

  @Override
  public Mono<Product> findByInternalId(String internalId) {
    try {
      return productOutputPort.findByInternalId(internalId);
    } catch (Exception e){
      return Mono.error(new ProductException(e));
    }
  }

  @Override
  public Flux<Product> sortByMetricsWeights(Map<String, String> requestParams) {
    try {
      return productOutputPort.sortByMetricsWeights(
        getMetricWeights(requestParams),
        getPageable(requestParams));
    } catch (Exception e) {
      return Flux.error(new ProductException(e));
    }
  }

  private List<MetricWeight> getMetricWeights(Map<String, String> requestParams) {
    return requestParams.entrySet().stream()
      .filter(param -> Metrics.getMetrics(param.getKey()) != Metrics.UNKNOWN)
      .map(param -> new MetricWeight(Metrics.getMetrics(param.getKey()), Double.parseDouble(param.getValue())))
      .toList();
  }

  private Pageable getPageable(Map<String, String> requestParams) {
    int page = Integer.parseInt(Optional.ofNullable(requestParams.get("page")).orElse("0"));
    int size = Integer.parseInt(Optional.ofNullable(requestParams.get("size")).orElse("10"));
    return PageRequest.of(page, size);
  }
}

