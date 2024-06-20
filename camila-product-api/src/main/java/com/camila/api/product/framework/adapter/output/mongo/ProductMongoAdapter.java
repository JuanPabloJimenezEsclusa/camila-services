package com.camila.api.product.framework.adapter.output.mongo;

import com.camila.api.product.application.port.output.ProductOutputPort;
import com.camila.api.product.domain.MetricWeight;
import com.camila.api.product.domain.Product;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.camila.api.product.framework.adapter.output.mongo.ProductSorterHelper.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

/**
 * The type Product mongo adapter.
 */
@Repository
@Qualifier(value = "productMongo")
class ProductMongoAdapter implements ProductOutputPort {

  private final ProductMongoRepository productMongoRepository;
  private final ReactiveMongoOperations mongoOperations;
  private final ProductMapper mapper;

  /**
   * Instantiates a new Product mongo adapter.
   *
   * @param productMongoRepository the product mongo repository
   * @param mongoOperations        the mongo operations
   * @param mapper                 the mapper
   */
  public ProductMongoAdapter(ProductMongoRepository productMongoRepository,
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
        buildLimitOperation(paging))
        .withOptions(buildOptions()),
      ProductEntity.DOCUMENT_NAME,
      ProductEntity.class).map(mapper::toProduct);
  }
}
