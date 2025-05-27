# camila-product-api-test

> [Summary](#-summary)
  • [Context](#-context)
  • [Architecture](#-architecture)
  • [Usage](#-usage)
  • [Notes](#-notes)

## 📜 Summary

---

This project implements a comprehensive test suite for the `camila-product-api` service, covering various aspects of its functionality and performance.

## 📚 Context

---

### Test Types

| Type                       | Details                                                                                                                                                                                                                           |
|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Unit Tests                 | Utilize mocks and the `surefire` plugin to isolate and test individual components [UT]                                                                                                                                            |
| Integration Tests          | Employ embedded databases (`de.flapdoodle.embed.mongo` for MongoDB and [Test Containers](https://testcontainers.com/modules/couchbase/) for Couchbase) and the `failsafe` plugin to validate interactions between components [IT] |
| Architecture Tests         | Leverage the `ArchUnit` library to ensure adherence to architectural principles and best practices [AT]                                                                                                                           |
| Mutation Tests             | Employ the [Pitest](https://github.com/pitest/pitest-junit5-plugin.git) plugin to systematically mutate code and verify its resilience to changes                                                                                 |
| Behavioral Tests           | Utilize [Cucumber](https://cucumber.io/docs/guides/) to define scenarios that capture the desired behavior of the API from a user's perspective                                                                                   |
| Benchmark Tests (jmh)      | Leverage the [Java Microbenchmark Harness](https://github.com/openjdk/jmh) [JMH-T] to measure performance under controlled conditions                                                                                             |
| Performance Tests (jmeter) | Employ [JMeter](https://jmeter.apache.org) to simulate heavy user load and assess performance under stress                                                                                                                        |

## 🏗️ Architecture

---

```txt
📦api
 ┣ 📂architecture (Architecture tests)
 ┣ 📂benchmark (Benchmark tests)
 ┣ 📂behaviour (Behaviuor tests)
 ┣ 📂product
 ┃ ┣ 📂application
 ┃ ┃ ┗ 📂usecase (Unit tests)
 ┃ ┗ 📂infrastructure
 ┃   ┗ 📂adapter
 ┃     ┣ 📂input
 ┃     ┃ ┣ 📂rest (Integration tests) (Unit tests)
 ┃     ┃ ┣ 📂graphql (Integration tests) (Unit tests)
 ┃     ┃ ┣ 📂grpc (Integration tests) (Unit tests)
 ┃     ┃ ┣ 📂websocket (Integration tests) (Unit tests)
 ┃     ┃ ┗ 📂rsocket (Integration tests) (Unit tests)
 ┃     ┗ 📂output
 ┃       ┣ 📂mongo (Integration tests)
 ┃       ┗ 📂couchbase (Integration tests)
 ┗ 📜ProductApiApplicationTests.java
```

## 🛠️ Usage

---

> [Unit and Architecture Tests](#unit-and-architecture-tests)
  • [Unit Tests with AOT](#unit-tests-with-aot)
  • [Integration and Benchmark Tests](#integration-and-benchmark-tests)
  • [Mutation Tests](#mutation-tests)
  • [Behaviour Test](#behaviour-test)
  • [Code Analysis](#code-analysis)
  • [Performance Tests](#performance-tests)

### Unit and Architecture Tests

```bash
cd ../../
mvn clean test
```

```bash
cd ../../
gradle clean unitTest
```

> Report: [gradle-tests-suite](./../../build/reports/tests/test/index.html)

### Unit Tests with AOT

```bash
export SPRING_PROFILES_ACTIVE=loc
mvn spring-boot:process-test-aot \
  -Dspring-boot.run.jvmArguments="--add-opens=java.base/java.lang=ALL-UNNAMED"
```

### Integration and Benchmark Tests

```bash
unset SPRING_PROFILES_ACTIVE
mvn clean verify
```

> Report: [jacoco](./../../target/site/jacoco/index.html)

### Mutation Tests

```bash
mvn clean test -P pitest
```

> Report: [pit-reports](./../../target/pit-reports/index.html)

### Behaviour test

```bash
mvn clean test -Dtest=com.camila.api.behaviour.ProductBehaviourRunner
```

> Report: [cucumber-reports](./../../target/cucumber-reports/Cucumber.html)

```bash
gradle clean test --tests "com.camila.api.behaviour.ProductBehaviourRunner"
```

### Code Analysis

* Error Prone Analysis: [error-prone](https://github.com/google/error-prone)
* Dependency Check: [dependency-check-maven](https://jeremylong.github.io/DependencyCheck/dependency-check-maven/)
* Checkstyle: [maven-checkstyle-plugin](https://checkstyle.sourceforge.io/)
* SpotBugs: [spotbugs-maven-plugin](https://spotbugs.github.io/)
* PMD: [pmd-maven-plugin](https://pmd.github.io/)
* SonarQube: [sonar-maven-plugin](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-maven/)

```bash
# Unset Spring Profile
unset SPRING_PROFILES_ACTIVE
# Export GPG Passphrase to avoid prompt during build
export MAVEN_GPG_PASSPHRASE=
mvn -B clean verify site -P error-prone,quality-check | tee code-analysis.log
```

```bash
# Unset Spring Profile
unset SPRING_PROFILES_ACTIVE
# Export GPG Passphrase to avoid prompt during build
export MAVEN_GPG_PASSPHRASE=
gradle clean check checkstyleMain checkstyleTest spotbugsMain spotbugsTest dependencyCheckAnalyze | tee code-analysis.log
```

```bash
# Run SonarQube
export SONAR_TOKEN=
mvn sonar:sonar
```

> Report: 
>  - [site-project-info](./../../target/site/project-info.html)
>  - [sonar-qube.io](https://sonarcloud.io/summary/overall?id=JuanPabloJimenezEsclusa_camila-services&branch=main)

### Performance Tests

* Configuration: [Readme](./resources/scripts/jmeter)

```bash
# Optional: Set default values
export JMETER_TEST_PATH="./resources/scripts/jmeter/"
export THREADS=100
export RAMP_UP=20 
export LOOPS=5

export BASE_URL_PROTOCOL="http"
export BASE_URL="localhost"
export BASE_URL_PORT="8080"
export BASE_PATH="product-dev"

# Run the test plan
./resources/scripts/jmeter/run.sh
```

## 📝 Notes

---

### Data Generator

A random data generator is available to populate the database for performance testing:

[RandomDataGenerator](java/com/camila/api/product/infrastructure/adapter/output/RandomDataGenerator.java)
