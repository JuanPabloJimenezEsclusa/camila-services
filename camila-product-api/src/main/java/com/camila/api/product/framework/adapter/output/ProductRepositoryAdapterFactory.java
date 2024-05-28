package com.camila.api.product.framework.adapter.output;

import com.camila.api.product.application.port.output.ProductOutputPort;
import com.camila.api.product.domain.MetricWeight;
import com.camila.api.product.domain.Product;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * The type Product repository adapter factory.
 */
@Component
@Primary
class ProductRepositoryAdapterFactory implements ProductOutputPort {

  @Value("#{systemEnvironment['REPOSITORY_TECHNOLOGY'] ?: '${spring.application.repository.technology:mongo}'}")
  private String repositoryTechnology;

  private final ProductOutputPort productMongoAdapter;
  private final ProductOutputPort productCouchbaseAdapter;

  /**
   * Instantiates a new Product repository adapter factory.
   *
   * @param productMongoAdapter     the product mongo adapter
   * @param productCouchbaseAdapter the product couchbase adapter
   */
  ProductRepositoryAdapterFactory(@Qualifier(value = "productMongo") ProductOutputPort productMongoAdapter,
                                  @Qualifier(value = "productCouchbase") ProductOutputPort productCouchbaseAdapter) {
    this.productMongoAdapter = productMongoAdapter;
    this.productCouchbaseAdapter = productCouchbaseAdapter;
  }

  @Override
  public Mono<Product> findByInternalId(String internalId) {
    return getRepositoryAdapter().findByInternalId(internalId);
  }

  @Override
  public Flux<Product> sortByMetricsWeights(List<MetricWeight> metricsWeights, Pageable paging) {
    return getRepositoryAdapter().sortByMetricsWeights(metricsWeights, paging);
  }

  private ProductOutputPort getRepositoryAdapter() {
    if ("couchbase".equalsIgnoreCase(repositoryTechnology)) {
      return productCouchbaseAdapter;
    }
    return productMongoAdapter;
  }
}
