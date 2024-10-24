package com.camila.api.product.framework.adapter.output.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;
import java.util.Objects;

/**
 * The type Product entity.
 */
@Document(collection = ProductEntity.DOCUMENT_NAME)
record ProductEntity(
  @Id String id,
  @Indexed(unique = true) String internalId,
  @Indexed(unique = true) String name,
  String category,
  int salesUnits,
  Map<String, Integer> stock) {

  /**
   * The constant DOCUMENT_NAME.
   */
  public static final String DOCUMENT_NAME = "products";

  /**
   * Instantiates a new Product entity.
   *
   * @param id         the id
   * @param internalId the internal id
   * @param name       the name
   * @param category   the category
   * @param salesUnits the sales units
   * @param stock      the stock
   */
  public ProductEntity {
    Objects.requireNonNull(id);
    Objects.requireNonNull(internalId);
    Objects.requireNonNull(name);
    Objects.requireNonNull(category);
    Objects.requireNonNull(stock);
  }
}
