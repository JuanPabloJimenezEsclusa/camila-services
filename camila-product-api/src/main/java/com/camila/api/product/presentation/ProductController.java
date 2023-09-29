package com.camila.api.product.presentation;

import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.service.ProductService;
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
 * The type Product controller.
 */
@RestController
@RequestMapping(value = "/products")
@Validated
class ProductController {

  private final ProductService productService;

  /**
   * Instantiates a new Product controller.
   *
   * @param productService the product service
   */
  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  /**
   * Find by id response entity.
   *
   * @param internalId the internal id
   * @return the response entity
   */
  @GetMapping(value = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE })
  public ResponseEntity<Mono<Product>> findById(@PathVariable("id") String internalId) {
    return ResponseEntity.ok(productService.findByInternalId(internalId));
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
    return ResponseEntity.ok(productService.sortByMetricsWeights(requestParams));
  }
}
