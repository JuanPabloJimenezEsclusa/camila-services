package com.camila.api.product.infrastructure.adapter.output.couchbase;

import com.camila.api.product.infrastructure.adapter.output.couchbase.config.CouchbaseCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.couchbase.repository.Query;
import org.springframework.data.couchbase.repository.ReactiveCouchbaseRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The interface Product couchbase repository.
 */
@Conditional(CouchbaseCondition.class)
public interface ProductCouchbaseRepository extends ReactiveCouchbaseRepository<ProductCouchbaseEntity, String> {
  /**
   * Find by internal id.
   *
   * @param internalId the internal id
   * @return the product couchbase entity
   */
  @Query("""
      SELECT meta().id AS __id, p.internalId, p.name, p.category, p.salesUnits, p.stock, p.profitMargin, p.daysInStock
      FROM #{#n1ql.bucket}.`product`.`products` AS p
      WHERE p.internalId = $1
    """)
  Mono<ProductCouchbaseEntity> findByInternalId(String internalId);

  /**
   * Sort by metrics weights.
   *
   * @param salesWeight the sales weight
   * @param stockWeight the stock weight
   * @param profitMargin the profit margin
   * @param daysInStock the days in stock
   * @param limit the limit
   * @param offset the offset
   * @return the product couchbase entity flux
   */
  @Query("""
    SELECT
      meta().id AS __id, p.internalId, p.name, p.category, p.salesUnits, p.stock, p.profitMargin, p.daysInStock,
      ((p.salesUnits * $1) +
      ((ARRAY_SUM(ARRAY v FOR v IN OBJECT_VALUES(p.stock) END) / ARRAY_LENGTH(OBJECT_VALUES(p.stock))) * $2) +
      (p.profitMargin * $3) +
      (p.daysInStock * $4)) AS weightedScore
    FROM #{#n1ql.bucket}.`product`.`products` AS p
    GROUP BY meta().id, p.internalId, p.name, p.category, p.salesUnits, p.stock, p.profitMargin, p.daysInStock
    ORDER BY weightedScore DESC
    LIMIT $5 OFFSET $6
    """)
  Flux<ProductCouchbaseEntity> sortByMetricsWeights(double salesWeight, double stockWeight,
                                                    double profitMargin, double daysInStock,
                                                    long limit, long offset);
}
