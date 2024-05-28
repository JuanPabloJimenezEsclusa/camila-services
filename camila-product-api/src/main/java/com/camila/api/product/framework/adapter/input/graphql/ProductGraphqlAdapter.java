package com.camila.api.product.framework.adapter.input.graphql;

import com.camila.api.product.application.usercase.ProductUserCase;
import com.camila.api.product.domain.Product;
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

import java.util.Map;

/**
 * The type Product graphql adapter.
 */
@Controller
@Validated
class ProductGraphqlAdapter {
  private final ProductUserCase productUserCase;

  /**
   * Instantiates a new Product graphql adapter.
   *
   * @param productUserCase the product user case
   */
  ProductGraphqlAdapter(ProductUserCase productUserCase) {
    this.productUserCase = productUserCase;
  }

  /**
   * Find by id mono.
   *
   * @param internalId the internal id
   * @return the mono
   */
  @QueryMapping
  public Mono<Product> findById(@Argument(name = "internalId") String internalId) {
    return productUserCase.findByInternalId(internalId);
  }

  /**
   * Sort products flux.
   *
   * @param salesUnits the sales units
   * @param stock      the stock
   * @param page       the page
   * @param size       the size
   * @return the flux
   */
  @QueryMapping
  public Flux<Product> sortProducts(
    @Argument(name = "salesUnits") @Valid @Min(0L) @Max(1000L) @PositiveOrZero Float salesUnits,
    @Argument(name = "stock") @Valid @Min(0L) @Max(1000L) @PositiveOrZero Float stock,
    @Argument(name = "page") @Valid @Min(0L) @Max(1000L) @PositiveOrZero Integer page,
    @Argument(name = "size") @Valid @Min(0L) @Max(1000L) @PositiveOrZero Integer size) {
    var requestParams = Map.of(
      "salesUnits", salesUnits.toString(),
      "stock", stock.toString(),
      "page", page.toString(),
      "size", size.toString()
    );
    return productUserCase.sortByMetricsWeights(requestParams);
  }
}
