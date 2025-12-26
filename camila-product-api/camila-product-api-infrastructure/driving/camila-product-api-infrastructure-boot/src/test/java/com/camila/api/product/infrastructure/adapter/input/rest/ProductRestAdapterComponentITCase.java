package com.camila.api.product.infrastructure.adapter.input.rest;

import static org.instancio.Select.field;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.stream.Stream;

import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.usecase.ProductUseCase;
import com.camila.api.product.infrastructure.adapter.input.security.LocalSecurityConfig;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = {ProductRestAdapter.class})
@Import({RestExceptionHandler.class, LocalSecurityConfig.class, ProductDTOMapperImpl.class})
@DisplayName("[IT][ProductRestAdapter] Product rest adapter [component] test")
class ProductRestAdapterComponentITCase {

  @Autowired
  private WebTestClient webClient;

  @MockitoBean
  private ProductUseCase productUseCase;

  @MockitoSpyBean
  private ProductDTOMapper productDTOMapper;

  private static Stream<Arguments> sortProductsWeightParams() {
    // salesUnits, stock, profitMargin, daysInStock
    return Stream.of(
      arguments("0.0001", "0.9990", "0.0001", "0.0001"),
      arguments("0.5", "0.5", "0.0", "0.0"),
      arguments("0.900", "0.088", "0.001", "0.001"),
      arguments("0.0", "1.0", "0.0", "0.0"));
  }

  private static Stream<Arguments> paginationParams() {
    // page, size
    return Stream.of(
      arguments("0", "10"),
      arguments("1", "20"),
      arguments("5", "5"),
      arguments("10", "50"));
  }

  private static Stream<Arguments> invalidTestParams() {
    return Stream.of(
      arguments(Map.of("salesUnits", "12345678")),
      arguments(Map.of("salesUnits", "a.5")),
      arguments(Map.of("salesUnits", "0.1", "stock", "invalid")),
      arguments(Map.of("empty", "")),
      arguments(Map.of("test", "1x")),
      arguments(Map.of("test", ".5")),
      arguments(Map.of("test", "-1")),
      arguments(Map.of("test", "1.2.3")));
  }

  @Test
  @DisplayName("Should find product by internal ID")
  void shouldFindProductById() {
    // Given
    final var internalId = "1";
    final var product = Instancio.of(Product.class).set(field(Product::internalId), internalId).create();
    when(this.productUseCase.findByInternalId(internalId)).thenReturn(Mono.just(product));

    // When & Then
    this.webClient.get().uri("/products/{id}", internalId)
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).exchange().expectStatus().isOk()
      .expectBody(Product.class);

    verify(this.productUseCase).findByInternalId(internalId);
    verify(this.productDTOMapper).toProductDTO(product);
    verifyNoMoreInteractions(this.productUseCase, this.productDTOMapper);
  }

  @Test
  @DisplayName("Should handle product not found")
  void shouldHandleProductNotFound() {
    // Given
    final var internalId = "999";
    when(this.productUseCase.findByInternalId(internalId)).thenReturn(Mono.empty());

    // When & Then
    this.webClient.get().uri("/products/{id}", internalId)
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).exchange().expectStatus().isOk()
      .expectBody().isEmpty();

    verify(this.productUseCase).findByInternalId(internalId);
    verifyNoMoreInteractions(this.productUseCase);
    verifyNoInteractions(this.productDTOMapper);
  }

  @ParameterizedTest(name = "{index} -> with salesUnits={0}, stock={1}")
  @MethodSource("sortProductsWeightParams")
  @DisplayName("Should sort products with different weight parameters")
  void shouldSortProductsWithWeightParams(final String salesUnits, final String stock, final String profitMargin,
                                          final String daysInStock) {
    // Given
    final var uri = "/products?salesUnits={salesUnits}&stock={stock}&profitMargin={profitMargin}&daysInStock={daysInStock}";
    final var internalId = "1";
    final var requestParams = Map.of("salesUnits", salesUnits, "stock", stock, "profitMargin", profitMargin,
      "daysInStock", daysInStock);
    final var product = Instancio.of(Product.class).set(field(Product::internalId), internalId).create();
    when(this.productUseCase.sortByMetricsWeights(requestParams)).thenReturn(Flux.just(product));

    // When & Then
    this.webClient.get().uri(uri, salesUnits, stock, profitMargin, daysInStock)
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).exchange().expectStatus().isOk()
      .expectBody().jsonPath("$[0].internalId").isEqualTo(internalId);

    verify(this.productUseCase).sortByMetricsWeights(requestParams);
    verify(this.productDTOMapper).toProductDTO(product);
    verifyNoMoreInteractions(this.productUseCase, this.productDTOMapper);
  }

  @ParameterizedTest(name = "{index} -> with page={0}, size={1}")
  @MethodSource("paginationParams")
  @DisplayName("Should sort products with pagination parameters")
  void shouldSortProductsWithPaginationParams(final String page, final String size) {
    // Given
    final var internalId = "1";
    final var requestParams = Map.of("page", page, "size", size);
    final var product = Instancio.of(Product.class).set(field(Product::internalId), internalId).create();
    when(this.productUseCase.sortByMetricsWeights(requestParams)).thenReturn(Flux.just(product));

    // When & Then
    this.webClient.get().uri("/products?page={page}&size={size}", page, size)
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).exchange().expectStatus().isOk()
      .expectBody().jsonPath("$[0].internalId").isEqualTo(internalId);

    verify(this.productUseCase).sortByMetricsWeights(requestParams);
    verify(this.productDTOMapper).toProductDTO(product);
    verifyNoMoreInteractions(this.productUseCase, this.productDTOMapper);
  }

  @Test
  @DisplayName("Should handle empty result when sorting products")
  void shouldHandleEmptyResultWhenSortingProducts() {
    // Given
    final var uri = "/products?salesUnits={salesUnits}&stock={stock}&profitMargin={profitMargin}&daysInStock={daysInStock}";
    final var requestParams = Map.of("salesUnits", "0.5", "stock", "0.5", "profitMargin", "0.0", "daysInStock",
      "0.0");
    when(this.productUseCase.sortByMetricsWeights(requestParams)).thenReturn(Flux.empty());

    // When & Then
    this.webClient.get().uri(uri, "0.5", "0.5", "0.0", "0.0")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).exchange().expectStatus().isOk()
      .expectBody().json("[]");

    verify(this.productUseCase).sortByMetricsWeights(requestParams);
    verifyNoMoreInteractions(this.productUseCase);
    verifyNoInteractions(this.productDTOMapper);
  }

  @ParameterizedTest(name = "{index} -> with params={0}")
  @MethodSource("invalidTestParams")
  @DisplayName("Should handle validation error with invalid parameters")
  void shouldHandleValidationErrorWithInvalidParameters(final Map<String, String> params) {
    // When & Then
    this.webClient.get().uri(builder -> {
      builder.path("/products");
      params.forEach(builder::queryParam);
      return builder.build();
    }).header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).exchange().expectStatus().is4xxClientError();

    verifyNoInteractions(this.productUseCase, this.productDTOMapper);
  }
}