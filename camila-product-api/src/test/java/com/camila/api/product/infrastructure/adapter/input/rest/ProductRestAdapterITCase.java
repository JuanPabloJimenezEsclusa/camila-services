package com.camila.api.product.infrastructure.adapter.input.rest;

import com.camila.api.product.application.usecase.DefaultProductUseCase;
import com.camila.api.product.infrastructure.adapter.input.rest.config.LocalOpenAPIConfig;
import com.camila.api.product.infrastructure.adapter.input.rest.config.Oauth2OpenAPIConfig;
import com.camila.api.product.infrastructure.adapter.input.security.LocalSecurityConfig;
import com.camila.api.product.infrastructure.adapter.output.couchbase.ProductCouchbaseAdapter;
import com.camila.api.product.infrastructure.adapter.output.couchbase.ProductCouchbaseContainerConfig;
import com.camila.api.product.infrastructure.adapter.output.couchbase.ProductCouchbaseMapperImpl;
import com.camila.api.product.infrastructure.adapter.output.couchbase.config.CouchbaseEnabledConfig;
import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientHealthAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.graphql.reactive.GraphQlWebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.graphql.security.GraphQlWebFluxSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketMessagingAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketRequesterAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketServerAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketStrategiesAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.reactive.WebSocketReactiveAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.cloud.client.loadbalancer.LoadBalancerDefaultMappingsProviderAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(properties = {
  "spring.main.lazy-initialization=true",
  "repository.technology=couchbase"
})
/*
  Slicing spring configuration.
  Benefits:
  - Faster test execution: Only loads necessary components
  - Improved test isolation: Focus on specific layers/components
  - Greater control: Clear definition of test boundaries
  - Reduced complexity: Tests only load what they need
*/
@ImportAutoConfiguration(exclude = {
  // GraphQL
  GraphQlWebFluxAutoConfiguration.class,
  GraphQlWebFluxSecurityAutoConfiguration.class,
  // gRPC
  GrpcClientAutoConfiguration.class,
  GrpcClientHealthAutoConfiguration.class,
  GrpcServerFactoryAutoConfiguration.class,
  LoadBalancerDefaultMappingsProviderAutoConfiguration.class,
  // WebSocket
  WebSocketReactiveAutoConfiguration.class,
  // RSocket
  RSocketServerAutoConfiguration.class,
  RSocketStrategiesAutoConfiguration.class,
  RSocketMessagingAutoConfiguration.class,
  RSocketRequesterAutoConfiguration.class,
  // Reactive Web Components
  WebClientAutoConfiguration.class,
  ReactiveOAuth2ClientAutoConfiguration.class,
  ReactiveOAuth2ResourceServerAutoConfiguration.class,
  // Security Components
  ReactiveSecurityAutoConfiguration.class,
  ReactiveUserDetailsServiceAutoConfiguration.class,
  // mongo
  MongoReactiveDataAutoConfiguration.class,
  MongoReactiveRepositoriesAutoConfiguration.class,
  MongoReactiveAutoConfiguration.class,
  EmbeddedMongoAutoConfiguration.class
})
@Import({
  // Framework adapter input layer
  LocalOpenAPIConfig.class,
  Oauth2OpenAPIConfig.class,
  ProductRestAdapter.class,
  QueryParametersValidator.class,
  ProductDTOMapperImpl.class,
  RestExceptionHandler.class,
  // Security
  LocalSecurityConfig.class,
  // Application layer
  DefaultProductUseCase.class,
  // Framework adapter output layer
  CouchbaseEnabledConfig.class,
  ProductCouchbaseAdapter.class,
  ProductCouchbaseMapperImpl.class
})
@ExtendWith(ProductCouchbaseContainerConfig.class)
@DisplayName("[IT][ProductRestAdapter] Product rest adapter test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductRestAdapterITCase {

  @Autowired
  private WebTestClient webClient;

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
