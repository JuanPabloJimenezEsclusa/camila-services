package com.camila.api.product.framework.adapter.input.rest;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * The type Product rest adapter integration test.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("[IT][ProductRestAdapter] Product rest adapter test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductRestAdapterITCase {

  @Autowired
  private WebTestClient webClient;

  /**
   * Find by internal id.
   */
  @Test
  @DisplayName("[ProductRestAdapter] findByInternalId ok")
  @Order(1)
  void findByInternalId() {
    webClient.get().uri("/products/{id}", 4)
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.internalId").isEqualTo(4);
  }

  /**
   * Sort products with stock more weight.
   */
  @Test
  @DisplayName("[ProductRestAdapter] sort products with stock more weight")
  @Order(1)
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
  @DisplayName("[ProductRestAdapter] sort products with sales units more weight")
  @Order(1)
  void sortProductsWithSalesUnitsMoreWeight() {
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
  @DisplayName("[ProductRestAdapter] sortProducts with page filter")
  @Order(1)
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
  @DisplayName("[ProductRestAdapter] sortProducts with page out")
  @Order(1)
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
  @DisplayName("[ProductRestAdapter] sortProducts with constraint violation")
  @Order(1)
  void sortProductsWithConstraintViolation() {
    webClient.get().uri("/products?page={page}&size={size}", "X", "Y")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .exchange()
      .expectStatus().is4xxClientError()
      .expectBody()
      .isEmpty();
  }
}
