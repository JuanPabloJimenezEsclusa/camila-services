package com.camila.api.product.application.usecase;

import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.stream.Stream;

import com.camila.api.product.domain.exception.NotFoundException;
import com.camila.api.product.domain.exception.ProductException;
import com.camila.api.product.domain.model.AppliedWeights;
import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.port.ProductRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("[UT][DefaultProductUseCase] Default Product Use Case Unit Test")
class DefaultProductUseCaseUnitTest {

  @Mock
  private ProductRepository productRepository;

  @InjectMocks
  private DefaultProductUseCase productUseCase;

  private static Stream<Arguments> validProductIds() {
    // internalId
    return Stream.of(
      arguments(named("Standard ID", "123")),
      arguments(named("ID with special chars", "ABC-123")),
      arguments(named("ID with leading zeros", "00000")),
      arguments(named("Long ID", "9999999999")));
  }

  private static Stream<Arguments> sortScenarios() {
    // requestParams, expectedOffset, expectedLimit
    return Stream.of(
      arguments(named("Default pagination values",
          Map.of("salesUnits", "0.38", "stock", "0.60", "profitMargin", "0.01", "daysInStock", "0.01")),
        0L, 10L),
      arguments(named("Custom pagination values",
          Map.of("salesUnits", "0.75", "stock", "0.23", "profitMargin", "0.01", "daysInStock", "0.01",
            "page", "2", "size", "5")),
        10L, 5L),
      arguments(named("Filtering unknown metrics",
        Map.of("salesUnits", "1", "unknown", "1", "invalidMetric", "1")), 0L, 10L),
      arguments(named("Empty parameters", Map.of()), 0L, 10L),
      arguments(named("With page parameter only", Map.of("page", "3")), 30L, 10L),
      arguments(named("With size parameter only", Map.of("size", "25")), 0L, 25L),
      arguments(named("Zero page with custom size", Map.of("page", "0", "size", "15")), 0L, 15L));
  }

  private static Stream<Arguments> invalidSortParams() {
    // requestParams, expectedError
    return Stream.of(
      arguments(named("Invalid page parameter", Map.of("page", "invalid")), "Invalid page parameter: invalid"),
      arguments(named("Negative page parameter", Map.of("page", "-1")), "Page number cannot be negative"),
      arguments(named("Invalid size parameter", Map.of("size", "nan")), "Invalid size parameter: nan"),
      arguments(named("Zero size parameter", Map.of("size", "0")), "Page size must be greater than zero"),
      arguments(named("Negative size parameter", Map.of("size", "-10")),
        "Page size must be greater than zero"),
      arguments(named("Negative metric weight", Map.of("salesUnits", "-1")),
        "Weight for salesUnits must be non-negative"),
      arguments(named("Invalid metric weight", Map.of("profitMargin", "abc")),
        "Invalid weight value for profitMargin"));
  }

  @ParameterizedTest(name = "{index}: {0}")
  @MethodSource("validProductIds")
  @DisplayName("Should find product by internal ID")
  void shouldFindProductByInternalId(final String internalId) {
    // Given: A valid product ID and repository will return a product
    final var expectedProduct = Instancio.of(Product.class).create();
    when(this.productRepository.findByInternalId(internalId)).thenReturn(Mono.just(expectedProduct));

    // When: Finding product by internal ID
    // Then: Should return the expected product
    this.productUseCase.findByInternalId(internalId).as(StepVerifier::create).expectNext(expectedProduct)
      .verifyComplete();

    verify(this.productRepository).findByInternalId(internalId);
    verifyNoMoreInteractions(this.productRepository);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  "})
  @DisplayName("Should handle empty or null internal ID")
  void shouldHandleEmptyOrNullInternalId(final String internalId) {
    // Given: Repository returns empty when ID is blank or null
    when(this.productRepository.findByInternalId(internalId)).thenReturn(Mono.empty());

    // When: Finding product with blank/null ID
    // Then: Should return empty mono
    this.productUseCase.findByInternalId(internalId).as(StepVerifier::create).verifyError(NotFoundException.class);

    verify(this.productRepository).findByInternalId(internalId);
    verifyNoMoreInteractions(this.productRepository);
  }

  @Test
  @DisplayName("Should handle exception when finding product by ID")
  void shouldHandleExceptionWhenFindingProductById() {
    // Given: Repository throws an exception
    final String internalId = "invalid";
    when(this.productRepository.findByInternalId(internalId))
      .thenReturn(Mono.error(new RuntimeException("Test exception")));

    // When: Finding product by internal ID
    // Then: Should wrap exception in ProductException
    this.productUseCase.findByInternalId(internalId).as(StepVerifier::create)
      .expectErrorMatches(throwable -> throwable instanceof ProductException
        && throwable.getCause().getMessage().equals("Test exception"))
      .verify();

    verify(this.productRepository).findByInternalId(internalId);
    verifyNoMoreInteractions(this.productRepository);
  }

  @ParameterizedTest(name = "{index}: {0}")
  @MethodSource("sortScenarios")
  @DisplayName("Should sort products by metrics weights with various parameters")
  void shouldSortProductsByMetricsWeights(final Map<String, String> requestParams, final long expectedOffset,
                                          final long expectedLimit) {
    // Given: Request parameters and expected results
    final var product = Instancio.of(Product.class).create();
    when(this.productRepository.sortByMetricsWeights(any(AppliedWeights.class), eq(expectedOffset),
      eq(expectedLimit))).thenReturn(Flux.just(product));

    // When: Sorting products
    // Then: Should return expected products
    this.productUseCase.sortByMetricsWeights(requestParams).as(StepVerifier::create).expectNext(product)
      .verifyComplete();

    verify(this.productRepository).sortByMetricsWeights(any(AppliedWeights.class), eq(expectedOffset),
      eq(expectedLimit));
    verifyNoMoreInteractions(this.productRepository);
  }

  @ParameterizedTest(name = "{index}: {0}")
  @MethodSource("invalidSortParams")
  @DisplayName("Should handle invalid parameters when sorting products")
  void shouldHandleInvalidSortParameters(final Map<String, String> requestParams, final String expectedError) {
    // Given: Invalid request parameters
    // When: Sorting products with invalid parameters
    // Then: Should throw exception with appropriate message
    this.productUseCase.sortByMetricsWeights(requestParams).as(StepVerifier::create)
      .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
        && throwable.getMessage().equals(expectedError))
      .verify();

    verifyNoInteractions(this.productRepository);
  }

  @Test
  @DisplayName("Should handle exception when no products match sort criteria")
  void shouldHandleExceptionWhenNoProductsMatchSortCriteria() {
    // Given: Repository returns empty flux
    final var requestParams = Map.of("salesUnits", "1.0");
    when(this.productRepository.sortByMetricsWeights(any(AppliedWeights.class), anyLong(), anyLong()))
      .thenReturn(Flux.empty());

    // When: Sorting products
    // Then: Should return empty flux
    this.productUseCase.sortByMetricsWeights(requestParams).as(StepVerifier::create)
      .verifyError(NotFoundException.class);

    verify(this.productRepository).sortByMetricsWeights(any(AppliedWeights.class), eq(0L), eq(10L));
    verifyNoMoreInteractions(this.productRepository);
  }

  @Test
  @DisplayName("Should handle exception when sorting products")
  void shouldHandleExceptionWhenSortingProducts() {
    // Given: Repository throws an exception
    final var requestParams = Map.of("salesUnits", "1");
    when(this.productRepository.sortByMetricsWeights(any(AppliedWeights.class), anyLong(), anyLong()))
      .thenReturn(Flux.error(new RuntimeException("Test exception")));

    // When: Sorting products
    // Then: Should wrap exception in ProductException
    this.productUseCase.sortByMetricsWeights(requestParams).as(StepVerifier::create)
      .expectErrorMatches(throwable -> throwable instanceof ProductException
        && throwable.getCause().getMessage().equals("Test exception"))
      .verify();

    verify(this.productRepository).sortByMetricsWeights(any(AppliedWeights.class), eq(0L), eq(10L));
    verifyNoMoreInteractions(this.productRepository);
  }

  @Test
  @DisplayName("Should not interact with repository when initializing")
  void constructorShouldNotInteractWithRepository() {
    // Given: A new instance of DefaultProductUseCase
    // When: No methods are called
    // Then: No interactions with repository
    verifyNoInteractions(this.productRepository);
  }
}
