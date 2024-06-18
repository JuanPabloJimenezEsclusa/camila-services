# camila-product-api-test

This project implements a comprehensive test suite for the `camila-product-api` service, covering various aspects of its functionality and performance.

## Types of Tests

| Test Type                  | Details                                                                                                                                                                                                                           |
|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Unit Tests                 | Utilize mocks and the `surefire` plugin to isolate and test individual components [UT]                                                                                                                                            |
| Integration Tests          | Employ embedded databases (`de.flapdoodle.embed.mongo` for MongoDB and [Test Containers](https://testcontainers.com/modules/couchbase/) for Couchbase) and the `failsafe` plugin to validate interactions between components [IT] |
| Architecture Tests         | Leverage the `ArchUnit` library to ensure adherence to architectural principles and best practices [AT]                                                                                                                           |
| Mutation Tests             | Employ the [Pitest](https://github.com/pitest/pitest-junit5-plugin.git) plugin to systematically mutate code and verify its resilience to changes                                                                                 |
| Behavioral Tests           | Utilize [Cucumber](https://cucumber.io/docs/guides/) to define scenarios that capture the desired behavior of the API from a user's perspective                                                                                   |
| Benchmark Tests (jmh)      | Leverage the [Java Microbenchmark Harness](https://github.com/openjdk/jmh) [JMH-T] to measure performance under controlled conditions                                                                                             |
| Performance Tests (jmeter) | Employ [JMeter](https://jmeter.apache.org) to simulate heavy user load and assess performance under stress                                                                                                                        |

## Architecture

```txt
📦api
 ┣ 📂architecture (Architecture tests)
 ┣ 📂benchmark (Benchmark tests)
 ┣ 📂behaviour (Behaviuor tests)
 ┣ 📂product
 ┃ ┣ 📂application
 ┃ ┃ ┗ 📂port
 ┃ ┃   ┗ 📂input (Unit tests)
 ┃ ┗ 📂framework
 ┃   ┗ 📂adapter
 ┃     ┣ 📂input
 ┃     ┃ ┣ 📂rest (Integration tests) (Unit tests)
 ┃     ┃ ┣ 📂graphql (Integration tests)
 ┃     ┃ ┣ 📂websocket (Integration tests)
 ┃     ┃ ┗ 📂rsocket (Integration tests)
 ┃     ┗ 📂output
 ┃       ┣ 📂mongo (Integration tests)
 ┃       ┗ 📂couchbase (Integration tests)
 ┗ 📜ProductApiApplicationTests.java
```

## Running Tests

### Unit and Architecture Tests

```bash
cd ../../
mvn clean test
```

### Unit Tests with AOT

```bash
export SPRING_PROFILES_ACTIVE=loc
mvn clean spring-boot:process-test-aot
```

### Integration and Benchmark Tests

```bash
mvn clean verify
```

### Mutation Tests

```bash
mvn clean test -P pitest
```

> Report: [./target/pit-reports/index.html](./../../target/pit-reports/index.html)

### Code Analysis

* Dependency Check: [dependency-check-maven](https://jeremylong.github.io/DependencyCheck/dependency-check-maven/)
* Error Prone Analysis: [error-prone](https://github.com/google/error-prone)

```bash
mvn clean verify site -P check-dependency,error-prone
```

> Report: [./target/site/project-info.html](./../../site/project-info.html)

### Performance Tests

* Configuration: [Readme](./resources/scripts/jmeter)

```bash
# Optional: Set default values
export JMETER_TEST_PATH="/tmp/results"
export THREADS=100
export RAMP_UP=20 
export LOOPS=5

# Run the test plan
./resources/scripts/jmeter/run.sh
```

## Data Generator

A random data generator is available to populate the database for performance testing:

[RandomDataGenerator](java/com/camila/api/product/framework/adapter/output/RandomDataGenerator.java)
