package com.camila.api.product.infrastructure.adapter.input.rest;

import com.camila.api.product.domain.model.Product;
import com.camila.api.product.infrastructure.adapter.input.rest.dto.ProductDTO;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * The interface Product dto mapper.
 */
@Mapper(
  componentModel = MappingConstants.ComponentModel.SPRING,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
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
  @Mapping(source = "profitMargin", target = "profitMargin")
  @Mapping(source = "daysInStock", target = "daysInStock")
  ProductDTO toProductDTO(Product product);
}
