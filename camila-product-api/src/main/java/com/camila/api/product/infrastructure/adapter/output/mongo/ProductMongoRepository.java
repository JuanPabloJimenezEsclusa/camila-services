package com.camila.api.product.infrastructure.adapter.output.mongo;

import com.camila.api.product.infrastructure.adapter.output.mongo.config.MongoCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 * The interface Product mongo repository.
 */
@Conditional(MongoCondition.class)
public interface ProductMongoRepository extends ReactiveCrudRepository<ProductMongoEntity, String> {
  /**
   * Find by internal id mono.
   *
   * @param internalId the internal id
   * @return the mono
   */
  @Query(value = "{ 'internalId' : ?0 }")
  Mono<ProductMongoEntity> findByInternalId(String internalId);
}
