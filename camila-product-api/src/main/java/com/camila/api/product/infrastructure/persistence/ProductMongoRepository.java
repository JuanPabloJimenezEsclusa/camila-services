package com.camila.api.product.infrastructure.persistence;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 * The interface Product mongo repository.
 */
interface ProductMongoRepository extends ReactiveCrudRepository<ProductEntity, String> {
  /**
   * Find by internal id mono.
   *
   * @param internalId the internal id
   * @return the mono
   */
  @Query(value = "{ 'internalId' : ?0 }")
  Mono<ProductEntity> findByInternalId(String internalId);
}
