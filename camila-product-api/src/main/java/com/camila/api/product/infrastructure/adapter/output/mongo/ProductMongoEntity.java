package com.camila.api.product.infrastructure.adapter.output.mongo;

import java.util.Map;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * The type Product mongo entity.
 */
@Document(collection = ProductMongoEntity.DOCUMENT_NAME)
public record ProductMongoEntity(
  @Id String id,
  @Indexed(unique = true) String internalId,
  @Indexed(unique = true) String name,
  String category,
  int salesUnits,
  Map<String, Integer> stock) {

  /**
   * The constant DOCUMENT_NAME.
   */
  static final String DOCUMENT_NAME = "products";

  /**
   * Instantiates a new Product mongo entity.
   *
   * @param id         the id
   * @param internalId the internal id
   * @param name       the name
   * @param category   the category
   * @param salesUnits the sales units
   * @param stock      the stock
   */
  public ProductMongoEntity {
    Objects.requireNonNull(id);
    Objects.requireNonNull(internalId);
    Objects.requireNonNull(name);
    Objects.requireNonNull(category);
    Objects.requireNonNull(stock);
  }
}
