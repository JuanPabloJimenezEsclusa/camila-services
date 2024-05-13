package com.camila.api.product.framework.adapter.input.rest;

import com.camila.api.product.domain.Product;
import com.camila.api.product.application.usercase.ProductUserCase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * The type Product rest adapter.
 */
@RestController
@RequestMapping(value = "/products")
@Validated
class ProductRestAdapter {

  private final ProductUserCase productUserCase;

  /**
   * Instantiates a new Product controller.
   *
   * @param productUserCase the product service
   */
  public ProductRestAdapter(ProductUserCase productUserCase) {
    this.productUserCase = productUserCase;
  }

  /**
   * Find by id response entity.
   *
   * @param internalId the internal id
   * @return the response entity
   */
  @GetMapping(value = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE })
  public ResponseEntity<Mono<Product>> findById(@PathVariable("id") String internalId) {
    return ResponseEntity.ok(productUserCase.findByInternalId(internalId));
  }

  /**
   * Sort products response entity.
   *
   * @param requestParams the request params
   * @return the response entity
   */
  @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
  public ResponseEntity<Flux<Product>> sortProducts(
    @RequestParam Map<@NotBlank String, @NotBlank @Min(0L) @Max(1000L) @PositiveOrZero String> requestParams) {
    return ResponseEntity.ok(productUserCase.sortByMetricsWeights(requestParams));
  }
}
