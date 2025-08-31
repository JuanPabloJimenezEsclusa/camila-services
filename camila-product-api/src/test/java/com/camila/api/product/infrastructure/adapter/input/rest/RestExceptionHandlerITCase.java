package com.camila.api.product.infrastructure.adapter.input.rest;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.camila.api.product.application.usecase.DefaultProductUseCase;
import com.camila.api.product.infrastructure.adapter.input.rest.config.LocalOpenAPIConfig;
import com.camila.api.product.infrastructure.adapter.input.security.LocalSecurityConfig;
import com.camila.api.product.infrastructure.adapter.output.couchbase.CouchbaseContainerConfig;
import com.camila.api.product.infrastructure.adapter.output.couchbase.ProductCouchbaseAdapter;
import com.camila.api.product.infrastructure.adapter.output.couchbase.ProductCouchbaseMapperImpl;
import com.camila.api.product.infrastructure.adapter.output.couchbase.config.CouchbaseConfig;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientHealthAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import org.springframework.boot.autoconfigure.websocket.reactive.WebSocketReactiveAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.cloud.client.loadbalancer.LoadBalancerDefaultMappingsProviderAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient(timeout = "10s")
@WebFluxTest(properties = {
  "spring.main.lazy-initialization=true",
  "repository.technology=couchbase"
})
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
  // mongo
  MongoReactiveDataAutoConfiguration.class,
  MongoReactiveRepositoriesAutoConfiguration.class,
  MongoReactiveAutoConfiguration.class
})
@Import({
  // Framework adapter input layer
  LocalOpenAPIConfig.class,
  ProductRestAdapter.class,
  QueryParametersValidator.class,
  ProductDTOMapperImpl.class,
  RestExceptionHandler.class,
  // Security
  LocalSecurityConfig.class,
  // Application layer
  DefaultProductUseCase.class,
  // Framework adapter output layer
  CouchbaseConfig.class,
  ProductCouchbaseAdapter.class,
  ProductCouchbaseMapperImpl.class
})
@DisplayName("[IT][RestExceptionHandler] Exception Handler Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RestExceptionHandlerITCase extends CouchbaseContainerConfig {

  @Autowired
  private WebTestClient webTestClient;

  private static final Random random = new SecureRandom();

  private static Stream<Arguments> exceptionTestCases() {
    return Stream.of(
      Arguments.of("Should return 200 OK", "/products/4", HttpStatus.OK),
      Arguments.of("Should return 204 NOT_CONTENT", "/products/99", HttpStatus.NO_CONTENT),
      Arguments.of("Should return 400 BAD_REQUEST", "/products/.", HttpStatus.BAD_REQUEST),
      Arguments.of("Should return 417 EXPECTATION_FAILED", "/products/test", HttpStatus.EXPECTATION_FAILED),
      Arguments.of("Should return 500 INTERNAL_SERVER_ERROR", "/products?salesUnits", HttpStatus.INTERNAL_SERVER_ERROR)
    );
  }

  private static String generateRandomString() {
    return random.ints(10, 0, 36)
      .mapToObj(i -> Integer.toString(i, 36))
      .collect(Collectors.joining())
      .toUpperCase(Locale.ROOT);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("exceptionTestCases")
  @DisplayName("[RestExceptionHandler] Should handle exceptions with correct status codes")
  @Order(6)
  void shouldHandleExceptionsWithCorrectStatusCodes(final String escenario,
                                                    final String endpoint,
                                                    final HttpStatus expectedStatus) {
    webTestClient.get()
      .uri(endpoint)
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .header("traceId", generateRandomString())
      .header("apiVersion", "1.0.0")
      .exchange()
      .expectStatus().isEqualTo(expectedStatus);
  }
}
