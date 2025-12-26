package com.camila.api.product.infrastructure.adapter.input.rest;

import static org.instancio.Select.field;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.usecase.ProductUseCase;
import com.camila.api.product.infrastructure.adapter.input.rest.dto.ProductDTO;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("[UT][ProductRestAdapter] Product REST Adapter Unit Tests")
class ProductRestAdapterUnitTest {

  @Mock
  private ProductUseCase productUseCase;

  @Mock
  private ProductDTOMapper productDTOMapper;

  @InjectMocks
  private ProductRestAdapter productRestAdapter;

  private static Stream<Arguments> sortProductsParams() {
    // salesUnits, stock, profitMargin, daysInStock, page, size
    return Stream.of(
      arguments("0.0", "1.0", "0.0", "0.0", "0", "10"),
      arguments("0.5", "0.5", "0.5", "0.5", "1", "20"),
      arguments("0.8", "0.2", "0.0", "0.0", "2", "50"),
      arguments("0.3", "0.7", "0.0", "0.0", "5", "15"));
  }

  @Test
  @DisplayName("Should find product by internal ID")
  void shouldFindProductById() {
    // Given
    final var internalId = "123";
    final var expectedProductDTO = Instancio.of(ProductDTO.class).set(field(ProductDTO::getId), "1").create();
    final var expectedProduct = Instancio.of(Product.class).create();
    when(this.productUseCase.findByInternalId(internalId)).thenReturn(Mono.just(expectedProduct));
    when(this.productDTOMapper.toProductDTO(expectedProduct)).thenReturn(expectedProductDTO);

    // When & Then
    this.productRestAdapter.findById(internalId, "", "", any()).as(StepVerifier::create)
      .expectNext(expectedProductDTO).verifyComplete();

    verify(this.productUseCase).findByInternalId(internalId);
    verify(this.productDTOMapper).toProductDTO(expectedProduct);
    verifyNoMoreInteractions(this.productUseCase, this.productDTOMapper);
  }

  @Test
  @DisplayName("Should return empty Mono when product not found")
  void shouldReturnEmptyMonoWhenProductNotFound() {
    // Given
    final var internalId = "non-existent";
    when(this.productUseCase.findByInternalId(internalId)).thenReturn(Mono.empty());

    // When & Then
    Objects.requireNonNull(this.productRestAdapter.findById(internalId, "", "", any())).as(StepVerifier::create)
      .verifyComplete();

    verify(this.productUseCase).findByInternalId(internalId);
    verifyNoMoreInteractions(this.productUseCase);
    verifyNoInteractions(this.productDTOMapper);
  }

  @ParameterizedTest(name = "{index} -> salesUnits={0}, stock={1}, profitMargin={2}, stock={3}, page={4}, size={5}")
  @MethodSource("sortProductsParams")
  @DisplayName("Should sort products with different parameters")
  void shouldSortProducts(final String salesUnits, final String stock, final String profitMargin,
                          final String daysInStock, final String page, final String size) {
    // Given
    final var requestParams = Map.of("salesUnits", salesUnits, "stock", stock, "profitMargin", profitMargin,
      "daysInStock", daysInStock, "page", page, "size", size);
    final var productDTO1 = Instancio.of(ProductDTO.class).create();
    final var productDTO2 = Instancio.of(ProductDTO.class).create();
    final var product1 = Instancio.of(Product.class).create();
    final var product2 = Instancio.of(Product.class).create();

    when(this.productUseCase.sortByMetricsWeights(requestParams)).thenReturn(Flux.just(product1, product2));
    when(this.productDTOMapper.toProductDTO(product1)).thenReturn(productDTO1);
    when(this.productDTOMapper.toProductDTO(product2)).thenReturn(productDTO2);

    // When & Then
    Objects.requireNonNull(this.productRestAdapter.sortProducts(requestParams, "", "", any()))
      .as(StepVerifier::create).expectNext(productDTO1).expectNext(productDTO2).verifyComplete();

    verify(this.productUseCase).sortByMetricsWeights(requestParams);
    verify(this.productDTOMapper).toProductDTO(product1);
    verify(this.productDTOMapper).toProductDTO(product2);
    verifyNoMoreInteractions(this.productUseCase, this.productDTOMapper);
  }

  @Test
  @DisplayName("Should return empty Flux when no products match sorting criteria")
  void shouldReturnEmptyFluxWhenNoProductsMatchSortingCriteria() {
    // Given
    final var requestParams = Map.of("salesUnits", "0.25", "stock", "0.25", "profitMargin", "0.25", "daysInStock",
      "0.25", "page", "0", "size", "10");

    when(this.productUseCase.sortByMetricsWeights(requestParams)).thenReturn(Flux.empty());

    // When & Then
    Objects.requireNonNull(this.productRestAdapter.sortProducts(requestParams, "", "", any()))
      .as(StepVerifier::create).verifyComplete();

    verify(this.productUseCase).sortByMetricsWeights(requestParams);
    verifyNoMoreInteractions(this.productUseCase);
    verifyNoInteractions(this.productDTOMapper);
  }
}
