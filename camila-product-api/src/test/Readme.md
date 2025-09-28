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

<details>
<summary><strong>Expand Context</strong></summary>

### Test Types

| Type                  | Details                                                                                                                                                                                                                              |
|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Contract Tests        | Uses [Stoplight](https://docs.stoplight.io) and [Redocly](https://redocly.com) to lint the API definition                                                                                                                            |
| Unit Tests            | Utilize mocks and the `surefire` plugin to isolate and test individual components [UT]                                                                                                                                               |
| Integration Tests     | Employ Test containers modules for: [MongoDB](https://testcontainers.com/modules/mongodb/) and [Couchbase](https://testcontainers.com/modules/couchbase/) and the `failsafe` plugin to validate interactions between components [IT] |
| Architecture Tests    | Leverage the `ArchUnit` library to ensure adherence to architectural principles and best practices [AT]                                                                                                                              |
| Mutation Tests        | Employ the [Pitest](https://github.com/pitest/pitest-junit5-plugin.git) plugin to systematically mutate code and verify its resilience to changes                                                                                    |
| Behavioral Tests      | Utilize [Cucumber](https://cucumber.io/docs/guides/) to define scenarios that capture the desired behavior of the API from a user's perspective                                                                                      |
| Benchmark Tests (jmh) | Leverage the [Java Microbenchmark Harness](https://github.com/openjdk/jmh) [JMH-T] to measure performance under controlled conditions                                                                                                |

</details>

## 🏗️ Architecture

---

<details>
<summary><strong>Expand Architecture</strong></summary>

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
 ┃       ┣ 📂cache (Integration tests)
 ┃       ┣ 📂mongo (Integration tests)
 ┃       ┗ 📂couchbase (Integration tests)
 ┗ 📜ProductApiApplicationTests.java
```

</details>

## 🛠️ Usage

---

<details>
<summary><strong>Expand Usage</strong></summary>

> [Contract Tests](#contract-tests)
  • [Unit and Architecture Tests](#unit-and-architecture-tests)
  • [Unit Tests with AOT](#unit-tests-with-aot)
  • [Integration and Benchmark Tests](#integration-and-benchmark-tests)
  • [Mutation Tests](#mutation-tests)
  • [Behaviour Test](#behaviour-test)
  • [Code Analysis](#code-analysis)

### Contract Tests

```bash
# https://docs.stoplight.io/docs/spectral/674b27b261c3c-overview
docker run --rm -it \
  --name="openapi-spectral-testing" \
  --network=host \
  --memory="256m" --memory-reservation="256m" --memory-swap="256m" --cpu-shares=500 \
  -v $PWD/src/main/resources:/tmp/resources/ \
  -v $PWD/../.spectral.yml:/tmp/spec/.spectral.yml \
  stoplight/spectral:latest lint "/tmp/resources/api/*.yml" --ruleset /tmp/spec/.spectral.yml --format stylish --fail-severity hint -v

# https://redocly.com/docs/cli/installation
docker run --rm -it \
  --name="openapi-redocly-testing" \
  --network=host \
  --memory="256m" --memory-reservation="256m" --memory-swap="256m" --cpu-shares=500 \
  -v $PWD/src/main/resources:/spec \
  redocly/cli:latest lint --format stylish --max-problems 20 "**/api/*.yml"
```

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
* Dependency Check: [dependency-check-maven](https://jeremylong.github.io/DependencyCheck/dependency-check-maven/), [nvd.nist.gov](https://nvd.nist.gov/)
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

# To open SpotBugs GUI
mvn -B spotbugs:gui -P quality-check
```

```bash
# Unset Spring Profile
unset SPRING_PROFILES_ACTIVE
# Export GPG Passphrase to avoid prompt during build
export MAVEN_GPG_PASSPHRASE=
gradle clean check checkstyleMain checkstyleTest spotbugsMain spotbugsTest | tee code-analysis.log
```

```bash
# Run SonarQube
export SONAR_TOKEN=
mvn sonar:sonar
```

> Report: 
>  - [site-project-info](./../../target/site/project-info.html)
>  - [sonar-qube.io](https://sonarcloud.io/summary/overall?id=JuanPabloJimenezEsclusa_camila-services&branch=main)

</details>

## 📝 Notes

---

### Data Generator

A random data generator is available to populate the database for performance testing:

[RandomDataGenerator](java/com/camila/api/product/infrastructure/adapter/output/RandomDataGenerator.java)
