package com.camila.api.product.infrastructure.adapter.output.mongo;

import java.util.Map;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = ProductMongoEntity.DOCUMENT_NAME)
public record ProductMongoEntity(
  @Id String id,
  @Indexed(unique = true) String internalId,
  @Indexed(unique = true) String name,
  String category,
  int salesUnits,
  Map<String, Integer> stock,
  double profitMargin,
  int daysInStock) {

  static final String DOCUMENT_NAME = "products";

  public ProductMongoEntity {
    Objects.requireNonNull(id);
    Objects.requireNonNull(internalId);
    Objects.requireNonNull(name);
    Objects.requireNonNull(category);
    Objects.requireNonNull(stock);
  }
}
