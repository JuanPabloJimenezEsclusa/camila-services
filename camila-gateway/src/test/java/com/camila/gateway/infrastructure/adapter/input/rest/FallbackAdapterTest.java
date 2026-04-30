package com.camila.gateway.infrastructure.adapter.input.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"PRODUCT_SERVER_URL=http://localhost:9999/", "spring.profiles.active=test"})
@DisplayName("[IT][FallbackAdapter] FailBack adapter test")
class FallbackAdapterTest {

  @Autowired
  private WebTestClient webClient;

  private static Stream<Arguments> gatewayFallbackArguments() {
    return Stream.of(
      arguments(502, "/product-dev/api/products?salesUnits=0.5&stock=0.5", """
        {"message":"Service Unavailable","details":"Circuit-breaker-fallback"}"""),
      arguments(502, "/product-dev/api/products/1", """
        {"message":"Service Unavailable","details":"Circuit-breaker-fallback"}"""));
  }

  @BeforeEach
  void setUp() {
    assertNotNull(this.webClient);
  }

  @ParameterizedTest(name = "{index} -> fallbackAllProducts | code: {0} | uri: {1} | result: {2}")
  @MethodSource("gatewayFallbackArguments")
  @DisplayName("[FallbackAdapter] fallback all product")
  void fallbackAllProducts(final int statusCode, final String uri, final String result) {
    this.webClient.get().uri(uri).exchange().expectStatus()
      .isEqualTo(statusCode).expectBody()
      .consumeWith(response -> assertEquals(result,
        new String(Objects.requireNonNull(response.getResponseBody()), StandardCharsets.UTF_8)));
  }
}
