package com.camila.api.product.domain.port;

import com.camila.api.product.domain.model.AppliedWeights;
import com.camila.api.product.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository {

  Mono<Product> findByInternalId(String internalId);

  Flux<Product> sortByMetricsWeights(AppliedWeights appliedWeights, long offset, long limit);
}
