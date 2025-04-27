package com.camila.api.product.infrastructure.adapter.input.graphql;

import java.util.Map;

import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.usecase.ProductUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The type Product graphql adapter.
 */
@Controller
@Validated
class ProductGraphqlAdapter {
  private final ProductUseCase productUseCase;

  /**
   * Instantiates a new Product graphql adapter.
   *
   * @param productUseCase the product user case
   */
  ProductGraphqlAdapter(final ProductUseCase productUseCase) {
    this.productUseCase = productUseCase;
  }

  /**
   * Find by id mono.
   *
   * @param internalId the internal id
   * @return the mono
   */
  @QueryMapping
  public Mono<Product> findById(@Argument(name = "internalId") final String internalId) {
    return productUseCase.findByInternalId(internalId);
  }

  /**
   * Sort products flux.
   *
   * @param salesUnits the sales units
   * @param stock the stock
   * @param page the page
   * @param size the size
   * @return the flux
   */
  @QueryMapping
  public Flux<Product> sortProducts(
    @Argument(name = "salesUnits") @Valid @Min(0L) @Max(1000L) @PositiveOrZero final Float salesUnits,
    @Argument(name = "stock") @Valid @Min(0L) @Max(1000L) @PositiveOrZero final Float stock,
    @Argument(name = "page") @Valid @Min(0L) @Max(1000L) @PositiveOrZero final Integer page,
    @Argument(name = "size") @Valid @Min(0L) @Max(1000L) @PositiveOrZero final Integer size) {
    var requestParams = Map.of(
      "salesUnits", salesUnits.toString(),
      "stock", stock.toString(),
      "page", page.toString(),
      "size", size.toString()
    );
    return productUseCase.sortByMetricsWeights(requestParams);
  }
}
