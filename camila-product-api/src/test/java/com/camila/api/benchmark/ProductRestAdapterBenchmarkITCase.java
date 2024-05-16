package com.camila.api.benchmark;

import com.camila.api.ProductApiApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.WarmupMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({ ProductApiApplication.class })
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@DisplayName("[JMH-T][ProductRestAdapter] Java benchmark tests")
@SuppressWarnings({"java:S5786"}) // JMH requires public test class
public class ProductRestAdapterBenchmarkITCase {
  private static final Random RANDOM_VALUES = new Random();
  private static WebTestClient webClient;

  @Autowired
  void setWebTestClient(WebTestClient webClient) {
    ProductRestAdapterBenchmarkITCase.webClient = webClient;
  }

  @Test
  @DisplayName("[ProductRestAdapter] Run benchmarks")
  void runBenchmarks() throws Exception {
    Options options = new OptionsBuilder()
      .include(".*findByInternalId.*|.*sortProductsWithStockMoreWeight.*")
      .warmupMode(WarmupMode.BULK)
      .shouldFailOnError(true)
      .shouldDoGC(true)
      .result("BenchmarkITCase.csv")
      .resultFormat(ResultFormatType.CSV)
      .build();

    Collection<RunResult> run = new Runner(options).run();
    Assertions.assertNotNull(run);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @Fork(value = 0, warmups = 0)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(time = 5, iterations = 1, timeUnit = TimeUnit.SECONDS, batchSize = 5)
  @Measurement(time = 5, iterations = 1, timeUnit = TimeUnit.SECONDS, batchSize = 5)
  @Threads(5)
  public void findByInternalId(Blackhole blackhole) {
    var optionalId = RANDOM_VALUES.ints(1, 6).findFirst();

    HttpStatusCode status = webClient.get().uri("/products/{id}", optionalId.orElseThrow())
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .returnResult().getStatus();

    Assertions.assertNotNull(status);
    blackhole.consume(status.toString());
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @Fork(value = 0, warmups = 0)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Warmup(time = 5, iterations = 1, timeUnit = TimeUnit.SECONDS, batchSize = 5)
  @Measurement(time = 5, iterations = 1, timeUnit = TimeUnit.SECONDS, batchSize = 5)
  @Threads(5)
  public void sortProductsWithStockMoreWeight(Blackhole blackhole) {
    var optionalSalesUnits = RANDOM_VALUES.ints(0, 100).findFirst();
    var salesUnits = optionalSalesUnits.orElseThrow();
    var stock = 100 - salesUnits;

    HttpStatusCode status = webClient.get()
      .uri("/products?salesUnits={salesUnits}&stock={stock}", salesUnits, stock)
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .returnResult().getStatus();

    Assertions.assertNotNull(status);
    blackhole.consume(status.toString());
  }
}
