package com.camila.api.product.infrastructure.adapter.output.mongo;

import static com.camila.api.product.infrastructure.adapter.output.mongo.ProductSorterHelper.buildLimitOperation;
import static com.camila.api.product.infrastructure.adapter.output.mongo.ProductSorterHelper.buildOptions;
import static com.camila.api.product.infrastructure.adapter.output.mongo.ProductSorterHelper.buildSkipOperation;
import static com.camila.api.product.infrastructure.adapter.output.mongo.ProductSorterHelper.buildSortOperation;
import static com.camila.api.product.infrastructure.adapter.output.mongo.ProductSorterHelper.buildWeightedScoreField;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

import java.util.List;

import com.camila.api.product.domain.model.MetricWeight;
import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.port.ProductRepository;
import com.camila.api.product.infrastructure.adapter.output.mongo.config.MongoCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The type Product mongo adapter.
 */
@Slf4j
@Conditional(MongoCondition.class)
@Repository
@Primary
public class ProductMongoAdapter implements ProductRepository {

  private final ProductMongoRepository productMongoRepository;
  private final ReactiveMongoOperations mongoOperations;
  private final ProductMongoMapper mapper;

  /**
   * Instantiates a new Product mongo adapter.
   *
   * @param productMongoRepository the product mongo repository
   * @param mongoOperations        the mongo operations
   * @param mapper                 the mapper
   */
  public ProductMongoAdapter(final ProductMongoRepository productMongoRepository,
                             final ReactiveMongoOperations mongoOperations,
                             final ProductMongoMapper mapper) {
    this.productMongoRepository = productMongoRepository;
    this.mongoOperations = mongoOperations;
    this.mapper = mapper;
  }

  @Override
  public Mono<Product> findByInternalId(final String internalId) {
    return productMongoRepository.findByInternalId(internalId).map(mapper::toProduct)
      .doOnNext(product -> log.debug("find By Id: {}", product));
  }

  @Override
  public Flux<Product> sortByMetricsWeights(final List<MetricWeight> metricsWeights, final long offset, final long limit) {
    log.debug("Sorting products by metrics weights: {}", metricsWeights);
    return mongoOperations.aggregate(
        newAggregation(
          buildWeightedScoreField(metricsWeights),
          buildSortOperation(),
          buildSkipOperation(offset),
          buildLimitOperation(limit))
          .withOptions(buildOptions()),
        ProductMongoEntity.DOCUMENT_NAME,
        ProductMongoEntity.class)
      .doOnNext(productEntity -> log.debug("Product sorted by metrics weights: {}", productEntity))
      .doOnError(throwable -> log.debug("Throwable sorting products by metrics weights: {}", throwable.getMessage()))
      .map(mapper::toProduct);
  }
}
