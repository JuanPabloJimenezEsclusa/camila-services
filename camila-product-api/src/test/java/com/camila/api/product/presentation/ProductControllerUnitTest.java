package com.camila.api.product.presentation;

import com.camila.api.product.domain.model.Product;
import com.camila.api.product.domain.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * The type Product controller unit test.
 */
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = { ProductController.class })
@Import({ RestExceptionHandler.class })
@DisplayName("[UT][ProductController] Product controller test")
class ProductControllerUnitTest {

  private static final String ID = "1";
  private Product product;

  @Autowired
  private WebTestClient webClient;

  @MockBean
  private ProductService productService;

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    assertNotNull(productService);

    product = new Product(ID, "1", "V-NECH BASIC SHIRT", "SHIRT", 100,
      Map.of("S", 4, "M", 9, "L", 0));
  }

  /**
   * Find by internal id.
   */
  @Test
  @DisplayName("[ProductController] findByInternalId ok")
  void findByInternalId() {
    // given
    Mono<Product> productMono = Mono.just(product);
    Mockito.when(productService.findByInternalId(ID)).thenReturn(productMono);
    // when
    webClient.get().uri("/products/{id}", ID)
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .exchange()
      .expectStatus().isOk()
      .expectBody(Product.class);
    // then
    Mockito.verify(productService, times(1)).findByInternalId(ID);
    Mockito.verifyNoMoreInteractions(productService);
  }

  /**
   * Sort products ok.
   */
  @Test
  @DisplayName("[ProductController] sortProducts ok")
  void sortProductsOk() {
    // given
    List<Product> products = List.of(product);
    Flux<Product> productFlux = Flux.fromIterable(products);
    Map<String, String> requestParams = Map.of("salesUnits", "0.001", "stock", "0.999");
    Mockito.when(productService.sortByMetricsWeights(requestParams)).thenReturn(productFlux);
    // when
    webClient.get().uri("/products?salesUnits={salesUnits}&stock={stock}", "0.001", "0.999")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$[0].id").isEqualTo(ID);
    // then
    Mockito.verify(productService, times(1)).sortByMetricsWeights(requestParams);
    Mockito.verifyNoMoreInteractions(productService);
  }

  /**
   * Sort products with page filter.
   */
  @Test
  @DisplayName("[ProductController] sortProducts with page filter ok")
  void sortProductsWithPageFilter() {
    //given
    List<Product> products = List.of(product);
    Flux<Product> productFlux = Flux.fromIterable(products);
    Map<String, String> requestParams = Map.of("page", "0", "size", "1");
    Mockito.when(productService.sortByMetricsWeights(requestParams)).thenReturn(productFlux);
    // when
    webClient.get().uri("/products?page={page}&size={size}", "0", "1")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$[0].id").isEqualTo(ID);
    //then
    Mockito.verify(productService, times(1)).sortByMetricsWeights(requestParams);
    Mockito.verifyNoMoreInteractions(productService);
  }
}