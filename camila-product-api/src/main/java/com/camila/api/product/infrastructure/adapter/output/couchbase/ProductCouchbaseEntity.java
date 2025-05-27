package com.camila.api.product.infrastructure.adapter.output.couchbase;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.repository.Collection;
import org.springframework.data.couchbase.repository.Scope;

/**
 * The type Product entity.
 */
@Document
@Scope("product")
@Collection("products")
public record ProductCouchbaseEntity(
  @Id String id,
  String internalId,
  String name,
  String category,
  int salesUnits,
  Map<String, Integer> stock,
  double profitMargin,
  int daysInStock
) {
}
