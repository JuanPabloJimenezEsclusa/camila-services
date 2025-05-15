package com.camila.gateway.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = {"PRODUCT_SERVER_URL=http://localhost:9999/"})
@DisplayName("[IT][FailBackController] FailBack controller test")
class FallbackControllerTest {

  @Autowired
  private WebTestClient webClient;

  @BeforeEach
  void setUp() {
    assertNotNull(webClient);
  }

  @ParameterizedTest(name = "{index} -> fallbackAllProducts | code: {0}  | uri: {1} | result: {2}")
  @CsvSource(value = {
    "502 | /product-dev/api/products?salesUnits=0.5&stock=0.5 | {\"message\":\"Service Unavailable\",\"details\":\"Circuit-breaker-fallback\"}",
    "502 | /product-dev/api/products/1                        | {\"message\":\"Service Unavailable\",\"details\":\"Circuit-breaker-fallback\"}"
  }, delimiter = '|')
  @DisplayName("[FailBackController] fallback all product")
  void fallbackAllProducts(int statusCode, String uri, String result) {
    webClient
      .get().uri(uri)
      .exchange()
      .expectStatus().isEqualTo(statusCode)
      .expectBody()
      .consumeWith(response ->
        assertEquals(result,
          new String(Objects.requireNonNull(response.getResponseBody()), StandardCharsets.UTF_8)));
  }
}
