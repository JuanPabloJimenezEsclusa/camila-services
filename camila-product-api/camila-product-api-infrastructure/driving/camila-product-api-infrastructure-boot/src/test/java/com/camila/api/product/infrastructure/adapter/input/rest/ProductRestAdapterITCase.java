package com.camila.api.product.infrastructure.adapter.input.rest;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.stream.Collectors;

import com.camila.api.product.application.usecase.DefaultProductUseCase;
import com.camila.api.product.infrastructure.adapter.input.security.SecurityConfig;
import com.camila.api.product.infrastructure.adapter.output.couchbase.CouchbaseContainerConfig;
import com.camila.api.product.infrastructure.adapter.output.couchbase.ProductCouchbaseAdapter;
import com.camila.api.product.infrastructure.adapter.output.couchbase.ProductCouchbaseMapperImpl;
import com.camila.api.product.infrastructure.adapter.output.couchbase.config.CouchbaseConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoReactiveAutoConfiguration;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.graphql.autoconfigure.reactive.GraphQlWebFluxAutoConfiguration;
import org.springframework.boot.graphql.autoconfigure.security.GraphQlWebFluxSecurityAutoConfiguration;
import org.springframework.boot.mongodb.autoconfigure.MongoReactiveAutoConfiguration;
import org.springframework.boot.rsocket.autoconfigure.RSocketMessagingAutoConfiguration;
import org.springframework.boot.rsocket.autoconfigure.RSocketRequesterAutoConfiguration;
import org.springframework.boot.rsocket.autoconfigure.RSocketServerAutoConfiguration;
import org.springframework.boot.rsocket.autoconfigure.RSocketStrategiesAutoConfiguration;
import org.springframework.boot.security.autoconfigure.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.reactive.ReactiveOAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.reactive.ReactiveOAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.boot.websocket.autoconfigure.servlet.WebSocketMessagingAutoConfiguration;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerDefaultMappingsProviderAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;
import org.springframework.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient(timeout = "10s")
@WebFluxTest(properties = {"spring.main.lazy-initialization=true", "repository.technology=couchbase"})
/*
 * Slicing spring configuration. Benefits:
 * - Faster test execution: Only loads necessary components
 * - Improved test isolation: Focus on specific layers/components
 * - Greater control: Clear definition of test boundaries
 * - Reduced complexity: Tests only load what they need
 */
@ImportAutoConfiguration(exclude = {
  // GraphQL
  GraphQlWebFluxAutoConfiguration.class, GraphQlWebFluxSecurityAutoConfiguration.class,
  // gRPC
  GrpcServerAutoConfiguration.class,
  GrpcServerFactoryAutoConfiguration.class, LoadBalancerDefaultMappingsProviderAutoConfiguration.class,
  // WebSocket
  WebSocketMessagingAutoConfiguration.class,
  // RSocket
  RSocketServerAutoConfiguration.class, RSocketStrategiesAutoConfiguration.class,
  RSocketMessagingAutoConfiguration.class, RSocketRequesterAutoConfiguration.class,
  // Reactive Web Components
  ReactiveOAuth2ClientAutoConfiguration.class, ReactiveOAuth2ResourceServerAutoConfiguration.class,
  // Security Components
  ReactiveWebSecurityAutoConfiguration.class, ReactiveUserDetailsServiceAutoConfiguration.class,
  // mongo
  DataMongoReactiveAutoConfiguration.class, DataMongoReactiveRepositoriesAutoConfiguration.class,
  MongoReactiveAutoConfiguration.class})
@Import({
  // Framework adapter input layer
  ProductRestAdapter.class, ProductDTOMapperImpl.class, RestExceptionHandler.class,
  // Security
  SecurityConfig.class,
  // Application layer
  DefaultProductUseCase.class,
  // Framework adapter output layer
  CouchbaseConfig.class, ProductCouchbaseAdapter.class, ProductCouchbaseMapperImpl.class})
@DisplayName("[IT][ProductRestAdapter] Product rest adapter test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductRestAdapterITCase extends CouchbaseContainerConfig {

  private static final String SORT_PRODUCTS_URI = "/products?salesUnits="
    + "{salesUnits}&stock={stock}&profitMargin={profitMargin}&daysInStock={daysInStock}";
  private static final SecureRandom random = new SecureRandom();

  @Autowired
  private WebTestClient webClient;

  private static String generateRandomString() {
    return random.ints(10, 0, 36).mapToObj(i -> Integer.toString(i, 36)).collect(Collectors.joining())
      .toUpperCase(Locale.ROOT);
  }

  @Test
  @DisplayName("[ProductRestAdapter] findByInternalId ok")
  @Order(1)
  void findByInternalId() {
    this.webClient.get().uri("/products/{id}", 4).header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .header("X-Trace-Id", generateRandomString()).header("X-Api-Version", "1.0.0").exchange().expectStatus()
      .isOk().expectBody().jsonPath("$.internalId").isEqualTo(4);
  }

  @Test
  @DisplayName("[ProductRestAdapter] sort products with stock more weight")
  @Order(1)
  void sortProductsWithStockMoreWeight() {
    this.webClient.get().uri(SORT_PRODUCTS_URI, "0.0018", "0.9990", "0.0001", "0.0001")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .header("X-Trace-Id", generateRandomString()).header("X-Api-Version", "1.0.0").exchange().expectStatus()
      .isOk().expectBody().jsonPath("$[0].internalId").isEqualTo(4).jsonPath("$[0].salesUnits").isEqualTo(3)
      .jsonPath("$[0].stock['S']").isEqualTo(25).jsonPath("$[0].stock['M']").isEqualTo(30)
      .jsonPath("$[0].stock['L']").isEqualTo(10).jsonPath("$[0].profitMargin").isEqualTo(0.12)
      .jsonPath("$[0].daysInStock").isEqualTo(65).jsonPath("$[0].stock['XL']").doesNotExist();
  }

  @Test
  @DisplayName("[ProductRestAdapter] sort products with sales units more weight")
  @Order(1)
  void sortProductsWithSalesUnitsMoreWeight() {
    this.webClient.get().uri(SORT_PRODUCTS_URI, "0.90", "0.08", "0.01", "0.01")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .header("X-Trace-Id", generateRandomString()).header("X-Api-Version", "1.0.0").exchange().expectStatus()
      .isOk().expectBody().jsonPath("$[0].internalId").isEqualTo(5).jsonPath("$[0].salesUnits").isEqualTo(650)
      .jsonPath("$[0].stock['S']").isEqualTo(0).jsonPath("$[0].stock['M']").isEqualTo(1)
      .jsonPath("$[0].stock['L']").isEqualTo(0).jsonPath("$[0].profitMargin").isEqualTo(0.17)
      .jsonPath("$[0].daysInStock").isEqualTo(31);
  }

  @Test
  @DisplayName("[ProductRestAdapter] sortProducts with page filter")
  @Order(1)
  void sortProductsWithPageFilter() {
    this.webClient.get().uri("/products?page={page}&size={size}", "5", "1")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .header("X-Trace-Id", generateRandomString()).header("X-Api-Version", "1.0.0").exchange().expectStatus()
      .isOk().expectBody().jsonPath("$[0].name").isEqualTo("PLEATED T-SHIRT");
  }

  @Test
  @DisplayName("[ProductRestAdapter] sortProducts with page out")
  @Order(1)
  void sortProductsWithPageOut() {
    this.webClient.get().uri("/products?page={page}&size={size}", "1", "10")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .header("X-Trace-Id", generateRandomString()).header("X-Api-Version", "1.0.0").exchange().expectStatus()
      .isNoContent().expectBody().jsonPath("$.status").isEqualTo(HttpStatus.NO_CONTENT.value());
  }

  @Test
  @DisplayName("[ProductRestAdapter] sortProducts with constraint violation")
  @Order(1)
  void sortProductsWithConstraintViolation() {
    this.webClient.get().uri("/products?page={page}&size={size}", "X", "Y")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .header("X-Trace-Id", generateRandomString()).header("X-Api-Version", "1.0.0").exchange().expectStatus()
      .is4xxClientError().expectBody().jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value());
  }
}
