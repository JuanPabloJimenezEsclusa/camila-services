package com.camila.api.product.framework.adapter.input.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Map;

/**
 * The type Product graphql adapter it case.
 */
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureGraphQlTester
@DisplayName("[IT][ProductGraphqlAdapter] Product graphql adapter test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductGraphqlAdapterITCase {
  private static final String ERROR_EXPECTED_VALUE = "Variable 'salesUnits' has an invalid value: " +
    "Expected a Number input, but it was a 'String'";

  @Autowired
  private WebTestClient webClient;

  /**
   * Find by id.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  @DisplayName("[ProductGraphqlAdapter] findByInternalId ok")
  @Order(2)
  void findById() throws JsonProcessingException {
    var query = """
      query findById($internalId: ID) {
        findById(internalId: $internalId) {
          id, internalId, category, name, salesUnits, stock
        }
      }
      """;

    Map<String, Object> variables = Map.of("internalId", "1");
    var body = new QueryData(query, variables);

    webClient.post().uri("/graphql")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .body(BodyInserters.fromValue(body))
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.data").isMap()
      .jsonPath("$.data[*].internalId").isEqualTo("1");
  }

  /**
   * Sort products with stock more weight.
   */
  @Test
  @DisplayName("[ProductGraphqlAdapter] sort products with stock more weight")
  @Order(2)
  void sortProductsWithStockMoreWeight() {
    var query = """
      query sortProducts($salesUnits: Float, $stock: Float, $withDetails: Boolean!) {
        sortProducts(salesUnits: $salesUnits, stock: $stock) {
          id @include(if: $withDetails)
          internalId @include(if: $withDetails)
          category @include(if: $withDetails)
          name
          salesUnits
          stock
        }
      }
      """;

    Map<String, Object> variables = Map.of(
      "salesUnits", 0.001,
      "stock", 0.999,
      "withDetails", false);
    var body = new QueryData(query, variables);

    webClient.post().uri("/graphql")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .body(BodyInserters.fromValue(body))
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.data").isMap()
      .jsonPath("$.data[*]").isArray()
      .jsonPath("$.data.sortProducts[0].salesUnits").isEqualTo(3)
      .jsonPath("$.data.sortProducts[0].stock['S']").isEqualTo(25)
      .jsonPath("$.data.sortProducts[0].stock['M']").isEqualTo(30)
      .jsonPath("$.data.sortProducts[0].stock['L']").isEqualTo(10)
      .jsonPath("$.data.sortProducts[0].stock['XL']").doesNotExist();
  }

  /**
   * Sort products with page filter.
   */
  @Test
  @DisplayName("[ProductGraphqlAdapter] sortProducts with page filter")
  @Order(2)
  void sortProductsWithPageFilter() {
    var query = """
      query sortProducts($salesUnits: Float, $stock: Float, $page: Int, $size: Int!) {
        sortProducts(salesUnits: $salesUnits, stock: $stock, page: $page, size: $size) {
          name
          salesUnits
          stock
        }
      }
      """;

    Map<String, Object> variables = Map.of(
      "salesUnits", 0.001,
      "stock", 0.999,
      "page", 0,
      "size", 2);
    var body = new QueryData(query, variables);

    webClient.post().uri("/graphql")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .body(BodyInserters.fromValue(body))
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.data.sortProducts[0].name").isEqualTo("PLEATED T-SHIRT");
  }

  /**
   * Sort products with page out.
   */
  @Test
  @DisplayName("[ProductGraphqlAdapter] sortProducts with page out")
  @Order(2)
  void sortProductsWithPageOut() {
    var query = """
      query sortProducts($salesUnits: Float, $stock: Float, $page: Int, $size: Int) {
        sortProducts(salesUnits: $salesUnits, stock: $stock, page: $page, size: $size) { id }
      }
      """;

    Map<String, Object> variables = Map.of(
      "salesUnits", 0.001,
      "stock", 0.999,
      "page", 1,
      "size", 10);
    var body = new QueryData(query, variables);

    webClient.post().uri("/graphql")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .body(BodyInserters.fromValue(body))
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.data.sortProducts").isEmpty();
  }

  /**
   * Sort products with constraint violation.
   */
  @Test
  @DisplayName("[ProductGraphqlAdapter] sortProducts with constraint violation")
  @Order(2)
  void sortProductsWithConstraintViolation() {
    var query = """
      query sortProducts($salesUnits: Float, $stock: Float) {
        sortProducts(salesUnits: $salesUnits, stock: $stock) { id }
      }
      """;

    Map<String, Object> variables = Map.of(
      "salesUnits", "X",
      "stock", "Y");
    var body = new QueryData(query, variables);

    webClient.post().uri("/graphql")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .body(BodyInserters.fromValue(body))
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$.errors[0].message").isEqualTo(ERROR_EXPECTED_VALUE);
  }
}

/**
 * The type Query data.
 */
record QueryData (String query, Map<String, Object> variables) { }
