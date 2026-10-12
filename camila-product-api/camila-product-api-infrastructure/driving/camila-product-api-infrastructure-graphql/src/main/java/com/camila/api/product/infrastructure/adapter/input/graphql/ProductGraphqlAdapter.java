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

@Controller
@Validated
class ProductGraphqlAdapter {

  private final ProductUseCase productUseCase;

  ProductGraphqlAdapter(final ProductUseCase productUseCase) {
    this.productUseCase = productUseCase;
  }

  @QueryMapping
  public Mono<Product> findById(@Argument(name = "internalId") final String internalId) {
    return this.productUseCase.findByInternalId(internalId);
  }

  @QueryMapping
  public Flux<Product> sortProducts(
    @Argument(name = "salesUnits") @Valid @Min(0L) @Max(1L) @PositiveOrZero final Float salesUnits,
    @Argument(name = "stock") @Valid @Min(0L) @Max(1L) @PositiveOrZero final Float stock,
    @Argument(name = "profitMargin") @Valid @Min(0L) @Max(1L) @PositiveOrZero final Float profitMargin,
    @Argument(name = "daysInStock") @Valid @Min(0L) @Max(1L) @PositiveOrZero final Float daysInStock,
    @Argument(name = "page") @Valid @Min(0L) @Max(1000L) @PositiveOrZero final Integer page,
    @Argument(name = "size") @Valid @Min(0L) @Max(1000L) @PositiveOrZero final Integer size) {

    final var requestParams = Map.of("salesUnits", salesUnits.toString(), "stock", stock.toString(), "profitMargin",
      profitMargin.toString(), "daysInStock", daysInStock.toString(), "page", page.toString(), "size",
      size.toString());
    return this.productUseCase.sortByMetricsWeights(requestParams);
  }
}
