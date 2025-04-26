package com.camila.api.product.infrastructure.adapter.input.rest;

import java.util.Map;

import com.camila.api.product.domain.usecase.ProductUseCase;
import com.camila.api.product.infrastructure.adapter.input.rest.api.ProductsApi;
import com.camila.api.product.infrastructure.adapter.input.rest.dto.ProductDTO;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The type Product rest adapter.
 */
@RestController
@RequestMapping
@CrossOrigin
class ProductRestAdapter implements ProductsApi {
  private final QueryParametersValidator queryParametersValidator;
  private final ProductUseCase productUseCase;
  private final ProductDTOMapper productDTOMapper;

  /**
   * Instantiates a new Product rest adapter.
   *
   * @param queryParametersValidator the query parameters validator
   * @param productUseCase the product use case
   * @param productDTOMapper the product dto mapper
   */
  public ProductRestAdapter(final QueryParametersValidator queryParametersValidator,
                            final ProductUseCase productUseCase, final ProductDTOMapper productDTOMapper) {
    this.queryParametersValidator = queryParametersValidator;
    this.productUseCase = productUseCase;
    this.productDTOMapper = productDTOMapper;
  }

  @Override
  public Mono<ProductDTO> findById(final String internalId, final ServerWebExchange exchange) {
    return productUseCase.findByInternalId(internalId).map(productDTOMapper::toProductDTO);
  }

  @Override
  public Flux<ProductDTO> sortProducts(final Map<String, String> requestParams, final ServerWebExchange exchange) {
    return queryParametersValidator.validate(requestParams)
      .flatMapMany(validParams -> productUseCase.sortByMetricsWeights(validParams)
        .map(productDTOMapper::toProductDTO));
  }
}
