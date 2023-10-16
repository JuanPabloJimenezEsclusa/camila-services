package com.camila.api.product.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * The type Product controller integration test.
 */
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("[IT][ProductController] Product controller test")
class ProductControllerITCase {

  @Autowired
  private WebTestClient webClient;

  /**
   * Find by internal id.
   */
  @Test
  @DisplayName("[ProductController] findByInternalId ok")
  void findByInternalId() {
    webClient.get().uri("/products/{id}", 4)
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.internalId").isEqualTo(4);
  }

  /**
   * Sort products with stock more weihght.
   */
  @Test
  @DisplayName("[ProductController] sort products with stock more weight")
  void sortProductsWithStockMoreWeight() {
    webClient.get().uri("/products?salesUnits={salesUnits}&stock={stock}", "0.001", "0.999")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$[0].internalId").isEqualTo(4)
      .jsonPath("$[0].salesUnits").isEqualTo(3)
      .jsonPath("$[0].stock['S']").isEqualTo(25)
      .jsonPath("$[0].stock['M']").isEqualTo(30)
      .jsonPath("$[0].stock['L']").isEqualTo(10)
      .jsonPath("$[0].stock['XL']").doesNotExist();
  }

  /**
   * Sort products with sales units more weihght.
   */
  @Test
  @DisplayName("[ProductController] sort products with sales units more weight")
  void sortProductsWithSalesUnitsMoreWeihght() {
    webClient.get().uri("/products?salesUnits={salesUnits}&stock={stock}", "0.9", "0.1")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$[0].internalId").isEqualTo(5)
      .jsonPath("$[0].salesUnits").isEqualTo(650)
      .jsonPath("$[0].stock['S']").isEqualTo(0)
      .jsonPath("$[0].stock['M']").isEqualTo(1)
      .jsonPath("$[0].stock['L']").isEqualTo(0);
  }

  /**
   * Sort products with page filter.
   */
  @Test
  @DisplayName("[ProductController] sortProducts with page filter")
  void sortProductsWithPageFilter() {
    webClient.get().uri("/products?page={page}&size={size}", "5", "1")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$[0].name").isEqualTo("PLEATED T-SHIRT");
  }

  /**
   * Sort products with page out.
   */
  @Test
  @DisplayName("[ProductController] sortProducts with page out")
  void sortProductsWithPageOut() {
    webClient.get().uri("/products?page={page}&size={size}", "1", "10")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$").isEmpty();
  }

  /**
   * Sort products with constraint violation.
   */
  @Test
  @DisplayName("[ProductController] sortProducts with constraint violation")
  void sortProductsWithConstraintViolation() {
    webClient.get().uri("/products?page={page}&size={size}", "X", "Y")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .exchange()
      .expectStatus().is4xxClientError()
      .expectBody()
      .isEmpty();
  }
}