package com.camila.gateway.infrastructure.adapter.input.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = "spring.profiles.active=test")
@DisplayName("[IT][CircuitBreakerSmokeTest] Gateway circuit-breaker smoke test")
class CircuitBreakerSmokeTest {

  private static MockWebServer mockWebServer;

  @Autowired
  private WebTestClient webClient;

  @BeforeAll
  static void startServer() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
  }

  @AfterAll
  static void stopServer() throws IOException {
    if (mockWebServer != null) {
      mockWebServer.shutdown();
    }
  }

  @DynamicPropertySource
  static void registerProperties(final DynamicPropertyRegistry registry) {
    registry.add("PRODUCT_SERVER_URL", () -> mockWebServer.url("/").toString());
    registry.add("resilience4j.circuitbreaker.configs.default.minimumNumberOfCalls", () -> "1");
    registry.add("resilience4j.circuitbreaker.configs.default.slidingWindowSize", () -> "2");
    registry.add("resilience4j.circuitbreaker.configs.default.failureRateThreshold", () -> "50");
    registry.add("resilience4j.circuitbreaker.configs.default.waitDurationInOpenState", () -> "1s");
    registry.add("resilience4j.timelimiter.configs.default.timeoutDuration", () -> "500ms");
  }

  @Test
  void circuitBreakerFallbackInvoked() {
    // Enqueue a series of server errors so the downstream fails repeatedly
    IntStream.range(0, 10)
      .forEach(_ -> mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("server-error")));

    // Call the gateway a few times; due to retries and circuit rules, eventually
    // the fallback should be returned
    final var fallbackSeen = new AtomicBoolean(false);
    IntStream.range(0, 8).forEach(_ -> {
      final var result = this.webClient.get().uri("/product-dev/api/products/1").exchange().expectBody()
        .returnResult();

      if (result.getStatus().value() == 502) {
        final byte[] bodyBytes = result.getResponseBody();
        assertNotNull(bodyBytes, "response body should not be null");
        final String body = new String(bodyBytes, StandardCharsets.UTF_8);
        assertEquals("""
          {"message":"Service Unavailable","details":"Circuit-breaker-fallback"}""", body);
        fallbackSeen.set(true);
      }
    });

    assertTrue(fallbackSeen.get(), "Expected the gateway fallback to be invoked (received HTTP 502)");
  }
}
