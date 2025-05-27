package com.camila.api.product.infrastructure.adapter.output.couchbase;

import com.camila.api.product.domain.model.AppliedWeights;
import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.port.ProductRepository;
import com.camila.api.product.infrastructure.adapter.output.couchbase.config.CouchbaseCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The type Product couchbase adapter.
 */
@Slf4j
@Conditional(CouchbaseCondition.class)
@Repository
public class ProductCouchbaseAdapter implements ProductRepository {
  private final ProductCouchbaseRepository productCouchbaseRepository;
  private final ProductCouchbaseMapper mapper;

  /**
   * Instantiates a new Product couchbase adapter.
   *
   * @param productCouchbaseRepository the product couchbase repository
   * @param mapper the mapper
   */
  public ProductCouchbaseAdapter(final ProductCouchbaseRepository productCouchbaseRepository,
                                 final ProductCouchbaseMapper mapper) {
    this.productCouchbaseRepository = productCouchbaseRepository;
    this.mapper = mapper;
  }

  @Override
  public Mono<Product> findByInternalId(final String internalId) {
    return productCouchbaseRepository.findByInternalId(internalId)
      .doOnNext(result -> log.debug("couchbase.adapter.findByInternalId: {}", result))
      .doOnError(throwable -> log.debug("throwable -> couchbase.adapter.findByInternalId: {}", throwable.getMessage()))
      .map(mapper::toProduct);
  }

  @Override
  public Flux<Product> sortByMetricsWeights(final AppliedWeights appliedWeights, final long offset, final long limit) {
    return productCouchbaseRepository.sortByMetricsWeights(appliedWeights.salesUnitsWeight(),
        appliedWeights.stockWeight(), appliedWeights.profitMarginWeight(), appliedWeights.daysInStockWeight(),
        limit, offset)
      .doOnNext(result -> log.debug("couchbase.adapter.sortByMetricsWeights: {}", result))
      .doOnError(throwable -> log.debug("throwable -> couchbase.adapter.sortByMetricsWeights: {}", throwable.getMessage()))
      .map(mapper::toProduct);
  }
}
