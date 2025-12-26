package com.camila.api.benchmark;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import com.camila.api.ProductApiApplication;
import com.camila.api.product.infrastructure.adapter.output.mongo.MongoContainerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.WarmupMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = {"logging.level.com.camila.api.product=ERROR"})
@Import({ProductApiApplication.class})
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
@DisplayName("[JMH-T][ProductRestAdapter] Java benchmark tests")
@SuppressWarnings({"java:S5786"}) // JMH requires public test class
public class ProductRestAdapterBenchmarkITCase extends MongoContainerConfig {

  private static final SecureRandom RANDOM_VALUES = new SecureRandom();
  private static WebTestClient webClient;

  @Autowired
  void setWebTestClient(final WebTestClient webClient) {
    ProductRestAdapterBenchmarkITCase.webClient = webClient;
  }

  @Test
  @DisplayName("[ProductRestAdapter] Run benchmarks")
  void runBenchmarks() throws Exception {
    final var options = new OptionsBuilder().include(".*findByInternalId.*|.*sortProductsWithStockMoreWeight.*")
      .warmupMode(WarmupMode.BULK).shouldFailOnError(true).shouldDoGC(true).result("BenchmarkITCase.csv")
      .resultFormat(ResultFormatType.CSV).build();

    final Collection<RunResult> run = new Runner(options).run();
    Assertions.assertFalse(run.isEmpty());
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @Fork(value = 0, warmups = 0)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Warmup(time = 5, iterations = 1, timeUnit = TimeUnit.SECONDS, batchSize = 1)
  @Measurement(time = 15, iterations = 1, timeUnit = TimeUnit.SECONDS, batchSize = 1)
  @Threads(5)
  public void findByInternalId(final Blackhole blackhole) {
    final var optionalId = RANDOM_VALUES.ints(1, 6).findFirst();

    final HttpStatusCode status = webClient.get()
      .uri("/products/{id}", optionalId.orElseThrow())
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).exchange().expectStatus().isOk()
      .expectBody().returnResult().getStatus();

    Assertions.assertNotNull(status);
    blackhole.consume(status.value());
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @Fork(value = 0, warmups = 0)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Warmup(time = 5, iterations = 1, timeUnit = TimeUnit.SECONDS, batchSize = 1)
  @Measurement(time = 15, iterations = 1, timeUnit = TimeUnit.SECONDS, batchSize = 1)
  @Threads(5)
  public void sortProductsWithStockMoreWeight(final Blackhole blackhole) {
    final var optionalSalesUnits = RANDOM_VALUES.ints(0, 100).findFirst();
    final var salesUnits = optionalSalesUnits.orElseThrow();
    final var stock = 100 - salesUnits;

    final HttpStatusCode status = webClient.get()
      .uri("/products?salesUnits={salesUnits}&stock={stock}", salesUnits, stock)
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).exchange().expectStatus().isOk()
      .expectBody().returnResult().getStatus();

    Assertions.assertNotNull(status);
    blackhole.consume(status.value());
  }
}
