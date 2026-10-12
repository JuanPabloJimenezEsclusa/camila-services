package com.camila.api.product.domain.usecase;

import java.util.Map;

import com.camila.api.product.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductUseCase {

  Mono<Product> findByInternalId(String internalId);

  Flux<Product> sortByMetricsWeights(Map<String, String> requestParams);
}
