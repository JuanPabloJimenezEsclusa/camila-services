package com.camila.api.product.infrastructure.adapter.input.rest;

import com.camila.api.product.domain.model.Product;
import com.camila.api.product.infrastructure.adapter.input.rest.dto.ProductDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The interface Product dto mapper.
 */
@Mapper(componentModel = "spring")
public interface ProductDTOMapper {
  /**
   * To product dto product dto.
   *
   * @param product the product
   * @return the product dto
   */
  @Mapping(source = "id", target = "id")
  @Mapping(source = "internalId", target = "internalId")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "category", target = "category")
  @Mapping(source = "salesUnits", target = "salesUnits")
  @Mapping(source = "stock", target = "stock")
  ProductDTO toProductDTO(Product product);
}
