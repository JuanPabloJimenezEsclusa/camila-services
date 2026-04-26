package com.camila.gateway.infrastructure.adapter.input.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.time.Duration;
import java.util.UUID;
import java.util.stream.IntStream;

import com.redis.testcontainers.RedisContainer;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = "spring.profiles.active=test")
@DisplayName("[IT][RateLimitConfig] Rate Limit Config test")
class RateLimiterIntegrationTest {

  private static final String REDIS_PASSWORD = "camila";

  private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:8.6.2-alpine");

  @Container
  static final RedisContainer redis = new RedisContainer(REDIS_IMAGE)
    .withStartupTimeout(Duration.ofMinutes(2L))
    .withCommand("redis-server --save 20 1 --requirepass %s".formatted(REDIS_PASSWORD))
    .withCreateContainerCmdModifier(cmd -> {
      cmd.withName("camila-gateway-redis-testing-%s".formatted(UUID.randomUUID()));
      if (cmd.getHostConfig() != null) {
        cmd.getHostConfig()
          .withMemory(2L * 1024 * 1024 * 1024)
          .withMemorySwap(2L * 1024 * 1024 * 1024)
          .withMemorySwappiness(0L)
          .withCpuCount(1L);
      }
    });

  static final MockWebServer mockServer;

  static {
    try {
      mockServer = new MockWebServer();
      mockServer.start();
    } catch (Exception e) {
      throw new RuntimeException("Failed to start MockWebServer", e);
    }
  }

  @LocalServerPort
  private int port;

  @AfterAll
  static void stopContainers() throws Exception {
    if (mockServer != null) {
      mockServer.shutdown();
    }
  }

  @DynamicPropertySource
  static void registerProperties(final DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    registry.add("spring.data.redis.password", () -> REDIS_PASSWORD);
    registry.add("PRODUCT_SERVER_URL", () -> mockServer.url("/").toString());
    // Aggressive limits for test
    registry.add("gateway.replenishRate", () -> "1");
    registry.add("gateway.burstCapacity", () -> "1");
    registry.add("gateway.requestedTokens", () -> "1");
    registry.add("rate.limiter.prefix", () -> "rltest");
  }

  @Test
  void rateLimiterShouldEnforceLimitsReactive() {
    IntStream.range(0, 200).forEach(_ -> mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("ok")));

    final var client = WebClient.create("http://localhost:%d".formatted(this.port));
    final var statusFlux = Flux.range(1, 100).flatMap(_ -> client.get().uri("/product-dev/api/products")
      .exchangeToMono(resp -> Mono.just(resp.statusCode().value())).onErrorResume(_ -> Mono.just(500)), 20);

    statusFlux.collectList().timeout(Duration.ofSeconds(60)).as(StepVerifier::create).assertNext(list -> {
      assertThat(list).isNotNull();
      final long count429 = list.stream().filter(code -> code == 429).count();
      assertThat(count429).as("Number of 429 responses").isGreaterThanOrEqualTo(1);
    }).expectComplete().verify(Duration.ofSeconds(70));
  }
}
