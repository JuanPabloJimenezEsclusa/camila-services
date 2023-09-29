package com.camila.api.product.infrastructure.persistence;

import com.camila.api.product.domain.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The interface Product mapper.
 */
@Mapper(componentModel = "spring")
interface ProductMapper {

  /**
   * To product product.
   *
   * @param productEntity the product entity
   * @return the product
   */
  @Mapping(source = "id", target = "id")
  @Mapping(source = "internalId", target = "internalId")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "category", target = "category")
  @Mapping(source = "salesUnits", target = "salesUnits")
  @Mapping(source = "stock", target = "stock")
  Product toProduct(ProductEntity productEntity);

}
