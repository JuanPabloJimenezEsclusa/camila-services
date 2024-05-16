package com.camila.api.product.framework.adapter.output.couchbase;

import com.camila.api.product.application.port.output.ProductOutputPort;
import com.camila.api.product.domain.MetricWeight;
import com.camila.api.product.domain.Metrics;
import com.camila.api.product.domain.Product;
import com.couchbase.client.java.ReactiveCluster;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryScanConsistency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * The type Product couchbase adapter.
 */
@Repository
@Qualifier(value = "productCouchbase")
@Slf4j
class ProductCouchbaseAdapter implements ProductOutputPort {
  private final ReactiveCluster cluster;

  /**
   * Instantiates a new Product couchbase adapter.
   *
   * @param cluster the cluster
   */
  public ProductCouchbaseAdapter(ReactiveCluster cluster) {
    this.cluster = cluster;
  }

  @Override
  public Mono<Product> findByInternalId(String internalId) {
    String statement = """
      SELECT meta().id, p.internalId, p.name, p.category, p.salesUnits, p.stock
      FROM `camila-product-bucket`.`product`.`products` AS p
      WHERE p.internalId = $1
    """;

    return cluster
      .query(statement, QueryOptions.queryOptions()
        .parameters(JsonArray.from(internalId))
        .scanConsistency(QueryScanConsistency.REQUEST_PLUS))
      .doOnNext(result -> log.debug("couchbase.adapter.findByInternalId: {}", result))
      .doOnError(throwable -> log.error("couchbase.adapter.findByInternalId: {}", throwable.getMessage()))
      .flatMap(result -> Mono.from(result.rowsAs(Product.class)));
  }

  @Override
  public Flux<Product> sortByMetricsWeights(List<MetricWeight> metricsWeights, Pageable paging) {
    String statement = """
      SELECT
        meta().id, p.internalId, p.name, p.category, p.salesUnits, p.stock,
        ((p.salesUnits * $1) + ((ARRAY_SUM(ARRAY v FOR v IN OBJECT_VALUES(p.stock) END) / ARRAY_LENGTH(OBJECT_VALUES(p.stock))) * $2)) AS weightedScore
      FROM `camila-product-bucket`.`product`.`products` AS p
      GROUP BY meta().id, p.internalId, p.name, p.category, p.salesUnits, p.stock
      ORDER BY weightedScore DESC
      LIMIT $3 OFFSET $4
    """;

    return cluster
      .query(statement, QueryOptions.queryOptions()
        .parameters(JsonArray.from(
          metricsWeights.stream().filter(metricWeight -> metricWeight.metric() == Metrics.SALES_UNITS).findFirst().orElseThrow().weight(),
          metricsWeights.stream().filter(metricWeight -> metricWeight.metric() == Metrics.STOCK).findFirst().orElseThrow().weight(),
          paging.toLimit().max(),
          paging.getOffset()))
        .scanConsistency(QueryScanConsistency.REQUEST_PLUS))
      .doOnNext(result -> log.debug("couchbase.adapter.sortByMetricsWeights: {}", result))
      .doOnError(throwable -> log.error("couchbase.adapter.sortByMetricsWeights: {}", throwable.getMessage()))
      .flatMapMany(result -> result.rowsAs(Product.class));
  }
}
