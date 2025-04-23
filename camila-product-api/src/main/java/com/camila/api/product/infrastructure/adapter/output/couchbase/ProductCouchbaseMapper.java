package com.camila.api.product.infrastructure.adapter.output.couchbase;

import com.camila.api.product.domain.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The interface Product couchbase mapper.
 */
@Mapper(componentModel = "spring")
public interface ProductCouchbaseMapper {
  /**
   * To product.
   *
   * @param productCouchbaseEntity the product couchbase entity
   * @return the product
   */
  @Mapping(source = "id", target = "id")
  @Mapping(source = "internalId", target = "internalId")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "category", target = "category")
  @Mapping(source = "salesUnits", target = "salesUnits")
  @Mapping(source = "stock", target = "stock")
  Product toProduct(ProductCouchbaseEntity productCouchbaseEntity);
}
