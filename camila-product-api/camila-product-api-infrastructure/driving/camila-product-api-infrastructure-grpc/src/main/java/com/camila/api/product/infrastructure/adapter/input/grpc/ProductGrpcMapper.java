package com.camila.api.product.infrastructure.adapter.input.grpc;

import org.mapstruct.AfterMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;

/**
 * The interface Product dto mapper.
 */
@Mapper(
  componentModel = MappingConstants.ComponentModel.SPRING,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ProductGrpcMapper {

  /**
   * Create product builder product . builder.
   *
   * @return the product . builder
   */
  @ObjectFactory
  default Product.Builder createProductBuilder() {
    return Product.newBuilder();
  }

  /**
   * To product.
   *
   * @param product the product
   * @return the product
   */
  default Product toProduct(com.camila.api.product.domain.model.Product product) {
    return toProductBuilder(product).build();
  }

  /**
   * Map stock.
   *
   * @param source the source
   * @param target the target
   */
  @AfterMapping
  default void mapStock(com.camila.api.product.domain.model.Product source, @MappingTarget Product.Builder target) {
    target.putAllStock(source.stock());
  }

  /**
   * To product builder product . builder.
   *
   * @param product the product
   * @return the product . builder
   */
  @Mapping(source = "id", target = "id")
  @Mapping(source = "internalId", target = "internalId")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "category", target = "category")
  @Mapping(source = "salesUnits", target = "salesUnits")
  @Mapping(target = "stock", ignore = true)
  @Mapping(source = "profitMargin", target = "profitMargin")
  @Mapping(source = "daysInStock", target = "daysInStock")
  Product.Builder toProductBuilder(com.camila.api.product.domain.model.Product product);
}
