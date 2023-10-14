package com.camila.api.product.infrastructure.persistence;

import com.camila.api.product.application.ProductRepository;
import com.camila.api.product.domain.model.MetricWeight;
import com.camila.api.product.domain.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.camila.api.product.infrastructure.persistence.ProductSorterHelper.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

/**
 * The type Product mongo repository.
 */
@Repository
class ProductMongoRepositoryImpl implements ProductRepository {

  private final ProductMongoRepository productMongoRepository;
  private final ReactiveMongoOperations mongoOperations;
  private final ProductMapper mapper;

  /**
   * Instantiates a new Product repository.
   *
   * @param productMongoRepository the product mongo repository
   * @param mongoOperations        the mongo operations
   * @param mapper                 the mapper
   */
  public ProductMongoRepositoryImpl(ProductMongoRepository productMongoRepository,
                                    ReactiveMongoOperations mongoOperations,
                                    ProductMapper mapper) {
    this.productMongoRepository = productMongoRepository;
    this.mongoOperations = mongoOperations;
    this.mapper = mapper;
  }

  @Override
  public Mono<Product> findByInternalId(String internalId) {
    return productMongoRepository.findByInternalId(internalId).map(mapper::toProduct);
  }

  @Override
  public Flux<Product> sortByMetricsWeights(List<MetricWeight> metricsWeights, Pageable paging) {
    return mongoOperations.aggregate(
      newAggregation(
        buildWeightedScoreField(metricsWeights),
        buildSortOperation(),
        buildSkipOperation(paging),
        buildLimitOperation(paging)),
      ProductEntity.DOCUMENT_NAME,
      ProductEntity.class).map(mapper::toProduct);
  }
}
