package com.camila.api.product.application.port.input;

import com.camila.api.product.application.exception.ProductException;
import com.camila.api.product.application.port.output.ProductOutputPort;
import com.camila.api.product.domain.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * The type Product service unit test.
 */
@ExtendWith(SpringExtension.class)
@DisplayName("[UT][ProductUserCase] Product service test")
class ProductUserCaseUnitTest {
  private static final String ID = "1";
  private Product product;

  @Mock
  private ProductOutputPort productOutputPort;

  @InjectMocks
  private ProductInputPort productInputPort;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    Assertions.assertNotNull(productInputPort);

    product = new Product(ID, "1", "V-NECH BASIC SHIRT", "SHIRT", 100,
      Map.of("S", 4, "M", 9, "L", 0));
  }

  /**
   * Find by id ok.
   */
  @Test
  @DisplayName("[ProductUserCase] findById ok")
  void findByIdOk() {
    // given
    Mono<Product> productMono = Mono.just(product);
    Mockito.when(productOutputPort.findByInternalId(ID)).thenReturn(productMono);
    // when
    Mono<Product> productResult = productInputPort.findByInternalId(ID);
    StepVerifier
      .create(productResult)
      .expectNext(product)
      .expectComplete()
      .verify();
    // then
    Mockito.verify(productOutputPort, times(1)).findByInternalId(ID);
    Mockito.verifyNoMoreInteractions(productOutputPort);
  }

  /**
   * Find by id with exception.
   */
  @Test
  @DisplayName("[ProductUserCase] findById with exception")
  void findByIdWithException() {
    // given
    String errorMessage = "internal error";
    Mockito.when(productOutputPort.findByInternalId(ID)).thenThrow(new RuntimeException(errorMessage));
    // when
    Mono<Product> productResult = productInputPort.findByInternalId(ID);
    StepVerifier
      .create(productResult)
      .expectErrorSatisfies(error -> {
        assertThat(error).isInstanceOf(ProductException.class);
        assertThat(error.getMessage()).contains(errorMessage);
      })
      .verify();
    // then
    Mockito.verify(productOutputPort, times(1)).findByInternalId(ID);
    Mockito.verifyNoMoreInteractions(productOutputPort);
  }

  /**
   * Sort by metrics weights ok.
   */
  @Test
  @DisplayName("[ProductUserCase] sortByMetricsWeights ok")
  void sortByMetricsWeightsOk() {
    // given
    List<Product> products = List.of(product);
    Flux<Product> productFlux = Flux.fromIterable(products);
    Map<String, String> requestParams = Map.of("salesUnits", "0.001", "stock", "0.999");
    Mockito.when(productOutputPort.sortByMetricsWeights(anyList(), any(Pageable.class))).thenReturn(productFlux);
    // when
    Flux<Product> productResults = productInputPort.sortByMetricsWeights(requestParams);
    StepVerifier
      .create(productResults)
      .expectNext(product)
      .expectComplete()
      .verify();
    // then
    Mockito.verify(productOutputPort, times(1))
      .sortByMetricsWeights(anyList(), any(Pageable.class));
    Mockito.verifyNoMoreInteractions(productOutputPort);
  }

  /**
   * Sort by metrics weights with pagination.
   */
  @Test
  @DisplayName("[ProductUserCase] sortByMetricsWeights with pagination ok")
  void sortByMetricsWeightsWithPagination() {
    // given
    List<Product> products = List.of(product);
    Flux<Product> productFlux = Flux.fromIterable(products);
    Map<String, String> requestParams = Map.of("page", "0", "size", "1");
    PageRequest pageRequest = PageRequest.of(0, 1);
    Mockito.when(productOutputPort.sortByMetricsWeights(anyList(), eq(pageRequest))).thenReturn(productFlux);
    // when
    Flux<Product> productResults = productInputPort.sortByMetricsWeights(requestParams);
    StepVerifier
      .create(productResults)
      .expectNext(product)
      .expectComplete()
      .verify();
    // then
    Mockito.verify(productOutputPort, times(1))
      .sortByMetricsWeights(anyList(), eq(pageRequest));
    Mockito.verifyNoMoreInteractions(productOutputPort);
  }

  /**
   * Sort by metrics weights with exception.
   */
  @Test
  @DisplayName("[ProductUserCase] sortByMetricsWeights with exception")
  void sortByMetricsWeightsWithException() {
    // given
    Map<String, String> requestParams = Map.of("page", "0", "size", "1");
    String errorMessage = "internal error";
    Mockito.when(productOutputPort.sortByMetricsWeights(anyList(), any(Pageable.class)))
      .thenThrow(new RuntimeException(errorMessage));
    // when
    Flux<Product> productResults = productInputPort.sortByMetricsWeights(requestParams);

    StepVerifier
      .create(productResults)
      .expectErrorSatisfies(error -> {
        assertThat(error).isInstanceOf(ProductException.class);
        assertThat(error.getMessage()).contains(errorMessage);
      })
      .verify();
    // then
    Mockito.verify(productOutputPort, times(1))
      .sortByMetricsWeights(anyList(), any(Pageable.class));
    Mockito.verifyNoMoreInteractions(productOutputPort);
  }
}