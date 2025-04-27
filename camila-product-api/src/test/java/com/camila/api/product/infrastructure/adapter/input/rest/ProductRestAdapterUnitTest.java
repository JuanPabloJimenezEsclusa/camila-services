package com.camila.api.product.infrastructure.adapter.input.rest;

import static org.instancio.Select.field;
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
  private QueryParametersValidator queryParametersValidator;

  @Mock
  private ProductUseCase productUseCase;

  @Mock
  private ProductDTOMapper productDTOMapper;

  @InjectMocks
  private ProductRestAdapter productRestAdapter;

  private static Stream<Arguments> sortProductsParams() {
    return Stream.of(
      Arguments.of("0.0", "1.0", "0", "10"),
      Arguments.of("0.5", "0.5", "1", "20"),
      Arguments.of("0.8", "0.2", "2", "50"),
      Arguments.of("0.3", "0.7", "5", "15")
    );
  }

  @Test
  @DisplayName("Should find product by internal ID")
  void shouldFindProductById() {
    // Given
    final var internalId = "123";
    final var expectedProductDTO = Instancio.of(ProductDTO.class).set(field(ProductDTO::getId), "1").create();
    final var expectedProduct = Instancio.of(Product.class).create();
    when(productUseCase.findByInternalId(internalId)).thenReturn(Mono.just(expectedProduct));
    when(productDTOMapper.toProductDTO(expectedProduct)).thenReturn(expectedProductDTO);

    // When & Then
    productRestAdapter.findById(internalId, "", "", any())
      .as(StepVerifier::create)
      .expectNext(expectedProductDTO)
      .verifyComplete();

    verify(productUseCase).findByInternalId(internalId);
    verify(productDTOMapper).toProductDTO(expectedProduct);
    verifyNoMoreInteractions(productUseCase, productDTOMapper);
    verifyNoInteractions(queryParametersValidator);
  }

  @Test
  @DisplayName("Should return empty Mono when product not found")
  void shouldReturnEmptyMonoWhenProductNotFound() {
    // Given
    final var internalId = "non-existent";
    when(productUseCase.findByInternalId(internalId)).thenReturn(Mono.empty());

    // When & Then
    Objects.requireNonNull(productRestAdapter.findById(internalId, "", "", any()))
      .as(StepVerifier::create)
      .verifyComplete();

    verify(productUseCase).findByInternalId(internalId);
    verifyNoMoreInteractions(productUseCase);
    verifyNoInteractions(queryParametersValidator, productDTOMapper);
  }

  @ParameterizedTest(name = "salesUnits={0}, stock={1}, page={2}, size={3}")
  @MethodSource("sortProductsParams")
  @DisplayName("Should sort products with different parameters")
  void shouldSortProducts(final String salesUnits, final String stock,
                          final String page, final String size) {
    // Given
    final var requestParams = Map.of(
      "salesUnits", salesUnits,
      "stock", stock,
      "page", page,
      "size", size
    );
    final var productDTO1 = Instancio.of(ProductDTO.class).set(field(ProductDTO::getId), "1").create();
    final var productDTO2 = Instancio.of(ProductDTO.class).set(field(ProductDTO::getId), "1").create();
    final var product1 = Instancio.of(Product.class).create();
    final var product2 = Instancio.of(Product.class).create();

    when(queryParametersValidator.validate(requestParams)).thenReturn(Mono.just(requestParams));
    when(productUseCase.sortByMetricsWeights(requestParams)).thenReturn(Flux.just(product1, product2));
    when(productDTOMapper.toProductDTO(product1)).thenReturn(productDTO1);
    when(productDTOMapper.toProductDTO(product2)).thenReturn(productDTO2);

    // When & Then
    Objects.requireNonNull(productRestAdapter.sortProducts(requestParams, "", "", any()))
      .as(StepVerifier::create)
      .expectNext(productDTO1)
      .expectNext(productDTO2)
      .verifyComplete();

    verify(queryParametersValidator).validate(requestParams);
    verify(productUseCase).sortByMetricsWeights(requestParams);
    verify(productDTOMapper).toProductDTO(product1);
    verify(productDTOMapper).toProductDTO(product2);
    verifyNoMoreInteractions(queryParametersValidator, productUseCase, productDTOMapper);
  }

  @Test
  @DisplayName("Should return empty Flux when no products match sorting criteria")
  void shouldReturnEmptyFluxWhenNoProductsMatchSortingCriteria() {
    // Given
    final var requestParams = Map.of(
      "salesUnits", "0.5",
      "stock", "0.5",
      "page", "0",
      "size", "10"
    );

    when(queryParametersValidator.validate(requestParams)).thenReturn(Mono.just(requestParams));
    when(productUseCase.sortByMetricsWeights(requestParams)).thenReturn(Flux.empty());

    // When & Then
    Objects.requireNonNull(productRestAdapter.sortProducts(requestParams, "", "", any()))
      .as(StepVerifier::create)
      .verifyComplete();

    verify(queryParametersValidator).validate(requestParams);
    verify(productUseCase).sortByMetricsWeights(requestParams);
    verifyNoMoreInteractions(queryParametersValidator, productUseCase);
    verifyNoInteractions(productDTOMapper);
  }
}
