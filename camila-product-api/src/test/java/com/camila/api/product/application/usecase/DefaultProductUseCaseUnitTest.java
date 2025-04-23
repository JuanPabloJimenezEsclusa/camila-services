package com.camila.api.product.application.usecase;

import com.camila.api.product.domain.exception.ProductException;
import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.port.ProductRepository;
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

import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[UT][DefaultProductUseCase] Default Product Use Case Unit Test")
class DefaultProductUseCaseUnitTest {

  @Mock
  private ProductRepository productRepository;

  @InjectMocks
  private DefaultProductUseCase productUseCase;

  private static Stream<Arguments> sortScenarios() {
    return Stream.of(
      Arguments.of(
        "Default pagination values",
        Map.of("salesUnits", "40", "stock", "60"),
        0L, 10L
      ),
      Arguments.of(
        "Custom pagination values",
        Map.of("salesUnits", "75", "stock", "25", "page", "2", "size", "5"),
        10L, 5L
      ),
      Arguments.of(
        "Filtering unknown metrics",
        Map.of("salesUnits", "100", "unknown", "50", "invalidMetric", "25"),
        0L, 10L
      ),
      Arguments.of(
        "Empty parameters",
        Map.of(),
        0L, 10L
      )
    );
  }

  @Test
  @DisplayName("Should find product by internal ID")
  void findByInternalId() {
    // Given: A valid product ID and repository will return a product
    final var internalId = "123";
    final var expectedProduct = Instancio.of(Product.class).create();
    when(productRepository.findByInternalId(internalId)).thenReturn(Mono.just(expectedProduct));

    // When: Finding product by internal ID
    // Then: Should return the expected product
    productUseCase.findByInternalId(internalId)
      .as(StepVerifier::create)
      .expectNext(expectedProduct)
      .verifyComplete();

    verify(productRepository).findByInternalId(internalId);
    verifyNoMoreInteractions(productRepository);
  }

  @Test
  @DisplayName("Should handle exception when finding product by ID")
  void findByInternalIdWithException() {
    // Given: Repository throws an exception
    final String internalId = "invalid";
    when(productRepository.findByInternalId(internalId)).thenThrow(new RuntimeException("Test exception"));

    // When: Finding product by internal ID
    // Then: Should wrap exception in ProductException
    productUseCase.findByInternalId(internalId)
      .as(StepVerifier::create)
      .expectErrorMatches(throwable ->
        throwable instanceof ProductException &&
          throwable.getCause().getMessage().equals("Test exception"))
      .verify();

    verify(productRepository).findByInternalId(internalId);
    verifyNoMoreInteractions(productRepository);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("sortScenarios")
  @DisplayName("Should sort products by metrics weights")
  void sortByMetricsWeights(final String scenario, final Map<String, String> requestParams,
                            final long expectedOffset, final long expectedLimit) {
    // Given: Request parameters and expected results
    final var product = Instancio.of(Product.class).create();
    when(productRepository.sortByMetricsWeights(anyList(), eq(expectedOffset), eq(expectedLimit)))
      .thenReturn(Flux.just(product));

    // When: Sorting products
    // Then: Should return expected products
    productUseCase.sortByMetricsWeights(requestParams)
      .as(StepVerifier::create)
      .expectNextMatches(productResult ->
        product.name().equals(productResult.name()) &&
          product.category().equals(productResult.category()) &&
          product.salesUnits() == productResult.salesUnits() &&
          product.stock().equals(productResult.stock()))
      .verifyComplete();

    verify(productRepository).sortByMetricsWeights(anyList(), eq(expectedOffset), eq(expectedLimit));
    verifyNoMoreInteractions(productRepository);
  }

  @Test
  @DisplayName("Should handle exception when sorting products")
  void sortByMetricsWeightsWithException() {
    // Given: Repository throws an exception
    final var requestParams = Map.of("salesUnits", "100");
    when(productRepository.sortByMetricsWeights(anyList(), anyLong(), anyLong()))
      .thenThrow(new RuntimeException("Test exception"));

    // When: Sorting products
    // Then: Should wrap exception in ProductException
    productUseCase.sortByMetricsWeights(requestParams)
      .as(StepVerifier::create)
      .expectErrorMatches(throwable ->
        throwable instanceof ProductException &&
          throwable.getCause().getMessage().equals("Test exception"))
      .verify();

    verify(productRepository).sortByMetricsWeights(any(), anyLong(), anyLong());
    verifyNoMoreInteractions(productRepository);
  }

  @Test
  @DisplayName("Should not interact with repository when initializing")
  void constructorShouldNotInteractWithRepository() {
    // Given: A new instance of DefaultProductUseCase
    // When: No methods are called
    // Then: No interactions with repository
    verifyNoInteractions(productRepository);
  }
}