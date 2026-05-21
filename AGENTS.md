# agents

## Project

Camila Services — microservices platform for product classification and ranking with a weighted algorithm.

- **Architecture**: Hexagonal (Ports & Adapters) + DDD
- **Paradigm**: Reactive (Project Reactor — `Mono`/`Flux`)
- **Language**: Java 25, Spring Boot 4.0.x
- **Build**: Maven 3.9.x (primary), Gradle 9.5.0 (experimental)

### Services

| Service              | Port | Purpose                       |
|----------------------|------|-------------------------------|
| camila-product-api   | 8080 | Product ranking & search API  |
| camila-gateway       | 8090 | API Gateway (circuit breaker) |
| camila-admin         | 8100 | Spring Boot Admin UI          |

camila-product-api protocols: REST (primary), GraphQL, WebSocket, RSocket, gRPC.

---

## Build & Test Commands

### Maven

```bash
# Build
mvn clean build                                     # Standard build
mvn clean package -DskipTests=true                  # Fast build (no tests)
mvn clean verify                                    # Full build + all tests
mvn -B build -P error-prone,quality-check           # Strict build (all checks)

# Test
mvn clean test                                      # Unit tests (*Test.java)
mvn clean verify                                    # Unit + integration (*ITCase.java)
mvn test -Dtest=ProductRestAdapterUnitTest          # Single class
mvn test -Dtest=ProductRestAdapterUnitTest#method   # Single method
mvn clean verify -P pitest                          # Mutation testing (Pitest)
mvn test -Dtest=ProductBehaviourRunner              # Cucumber BDD

# Quality
mvn checkstyle:check                                # Google Java Style
mvn spotbugs:check                                  # Static analysis
mvn clean verify site -P error-prone,quality-check  # Full quality report
mvn sonar:sonar                                     # SonarCloud

# Maintenance
mvn versions:display-dependency-updates             # Outdated dependencies
mvn rewrite:runNoFork -Popen-rewrite                # Automated refactoring

# Run / Docker
mvn spring-boot:run -pl camila-product-api          # Single service locally
mvn spring-boot:build-image                         # Docker image (Buildpacks)
```

### Gradle (experimental)

```bash
gradle build                     # Standard build
gradle clean build               # Clean build
gradle unitTest                  # Unit tests only
gradle clean check               # Full verification
gradle qualityBuild -PerrorProne # With error-prone
gradle aggregateJacoco           # Code coverage
```

---

## Architecture Rules

```
┌─────────────────────────────────┐
│        Domain Layer             │
│  Models, Ports, Use Cases       │
└────────┬───────────────┬────────┘
    ▲                   ▲
┌───┴─────────┐  ┌──────┴───────┐
│Input Adapters│  │Output Adapters│
│REST, GraphQL,│  │Mongo, Cache   │
│gRPC, WebSocket│  │Couchbase     │
└──────────────┘  └──────────────┘
```

- **Domain** (`camila-product-api-domain/`): Business logic, models, port interfaces. No framework/database imports.
- **Application** (`camila-product-api-application/`): Use cases, orchestration.
- **Infrastructure**:
  - Driving adapters: `infrastructure/driving/` (REST, GraphQL, gRPC, WebSocket, RSocket)
  - Driven adapters: `infrastructure/driven/` (MongoDB, Couchbase, Cache)

### Mandatory Principles

1. Domain MUST NOT import Spring, database, or framework classes
2. Infrastructure depends on domain — never the reverse
3. Port interfaces defined in domain, implemented in infrastructure
4. All I/O via reactive types (`Mono`/`Flux`) — NEVER call `.block()`

---

## Code Style

### Formatting

- 2-space indent (NO tabs), UTF-8, LF line endings
- Max 140 chars (checkstyle), aim for 120 (editorconfig)
- K&R braces (opening brace on same line), empty blocks: `{}`

### Imports

- Order: static imports → `java.*` → third-party → project
- Groups separated by blank lines, sorted alphabetically
- No wildcard imports (`import java.util.*` forbidden)

```java
import static org.mockito.Mockito.verify;

import java.util.Map;

import com.camila.api.product.domain.model.Product;
import org.springframework.stereotype.Service;
```

### Naming

| Element      | Convention                | Example                          |
|-------------|---------------------------|----------------------------------|
| Packages    | lowercase, no underscores | `com.camila.api.product`         |
| Classes     | PascalCase                | `ProductRestAdapter`             |
| Interfaces  | PascalCase, no `I` prefix | `ProductRepository`              |
| Methods     | camelCase                 | `findByInternalId`               |
| Variables   | camelCase                 | `internalId`                     |
| Constants   | UPPER_SNAKE_CASE          | `SORTED_REQUEST_PARAMS`          |
| Type params | single letter or `T` suffix | `T`, `ProductT`                |
| Test names  | snake_case allowed        | `should_find_product_by_id`      |

Allowed abbreviations (max 3 chars): DTO, API, OAUTH, ITC

### Types & Declarations

- **Use records** for immutable data (models, DTOs) — NO Lombok
- **Use `final`** on parameters and locals when possible
- **No raw types** — always `List<Product>`, never `List`
- **Reactive types**: `Mono<T>` for single values, `Flux<T>` for streams

```java
public record Product(
  String id, String internalId, String name, String category,
  int salesUnits, Map<String, Integer> stock,
  double profitMargin, int daysInStock
) {}
```

### Error Handling

- Reactive chains use `.onErrorMap()`, `.onErrorResume()`, `.switchIfEmpty()` — NOT try-catch
- Custom exceptions extend domain base exceptions (e.g. `NotFoundException extends ProductException`)
- Never catch generic `Exception` — catch specific types
- Log at appropriate level: `log.error()`, `log.warn()`, `log.info()`, `log.debug()`

```java
return repo.findByInternalId(id)
  .switchIfEmpty(Mono.error(new NotFoundException("Product not found: " + id)))
  .onErrorMap(DatabaseException.class, e -> new ProductException("DB error", e));
```

---

## Testing

### Test Types

| Type         | Pattern                    | Framework       | Command                        |
|-------------|---------------------------|-----------------|--------------------------------|
| Unit        | `*UnitTest.java`          | JUnit 5, Mockito | `mvn clean test`               |
| Integration | `*ITCase.java`            | Testcontainers   | `mvn clean verify`             |
| Architecture| `*ArchitectureTest.java`  | ArchUnit         | (included in verify)           |
| BDD         | `*BehaviourRunner.java`   | Cucumber         | `mvn test -Dtest=...Runner`    |
| Mutation    | —                         | Pitest           | `mvn clean verify -P pitest`   |
| Contract    | —                         | Spectral         | (CI only)                      |

### Test Structure

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("[UT][ProductRestAdapter] Product REST Adapter Unit Tests")
class ProductRestAdapterUnitTest {

  @Mock
  private ProductUseCase productUseCase;

  @InjectMocks
  private ProductRestAdapter adapter;

  @Test
  @DisplayName("Should find product by internal ID")
  void shouldFindProductById() {
    // Given
    final var id = "123";
    final var product = Instancio.of(Product.class).create();
    when(productUseCase.findByInternalId(id)).thenReturn(Mono.just(product));

    // When & Then
    adapter.findById(id, "", "", null)
      .as(StepVerifier::create)
      .expectNext(expectedDTO)
      .verifyComplete();

    verify(productUseCase).findByInternalId(id);
  }
}
```

### Rules

- **Given-When-Then** structure for all tests
- **StepVerifier** for reactive streams, **Instancio** for test data (avoid manual builders)
- **Always verify** mock interactions
- **@DisplayName** on every test class and method
- **Testcontainers reuse**: add `testcontainers.reuse.enable=true` to `~/.testcontainers.properties`

### Coverage

- Tool: JaCoCo, aggregated module: `camila-coverage-jacoco`
- Excludes: generated code, DTOs, configs, mappers
- Report: `target/site/jacoco/index.html`

---

## Documentation (Javadoc)

- **Public classes & methods**: must have Javadoc (except `@Override`, `@Test`)
- **Protected methods**: should have Javadoc
- Tags: `@param`, `@return`, `@throws`
- Format: first sentence = summary, then detailed description

```java
/**
 * Retrieves a product by its internal identifier.
 *
 * @param internalId the internal product identifier
 * @return a Mono containing the product if found
 */
Mono<Product> findByInternalId(String internalId);
```

---

## Configuration & Profiles

### Spring Profiles

| Profile          | Purpose                          |
|-----------------|----------------------------------|
| `loc`,`dev`     | Local development (ChaosMonkey)  |
| `dev`           | Docker Compose + Consul          |
| `int`           | Integration / K8s                |
| `pre`,`pro`     | Production (OAuth2)              |
| `local-compose` | Docker Compose + Dev Services    |

### Key Config

- **Virtual threads**: enabled by default (Java 25)
- **Security**: OAuth2 + Keycloak
- **Cache**: Redis (distributed) + Caffeine (local)
- **Observability**: Zipkin, Prometheus

### Database

MongoDB and Couchbase both supported. Set `repository.technology=mongodb` or `repository.technology=couchbase`. Adapters implement the same port interface.

---

## Local Development

### Prerequisites

JDK 25+, Maven 3.9.x, Docker 29+, Docker Compose 2.35.0+

### Start

```bash
cd camila-orchestrator/dev/compose && ./start.sh
```

### Access Points

| Service     | URL                                                      |
|------------|----------------------------------------------------------|
| Product API | http://localhost:8080/product-dev/api/swagger-ui.html    |
| Gateway     | http://localhost:8090/swagger-ui.html                     |
| Consul      | http://localhost:8500/ui/                                 |
| Admin       | http://localhost:8100/                                    |

Databases: MongoDB `:27017`, Couchbase `:8091`, Redis `:6379`

---

## Git Commits

- Present tense, imperative mood: `fix: resolve NPE in ProductRestAdapter`
- First line ≤72 chars
- Types: add, update, fix, refactor, test, docs, chore

---

## Code Quality Tools

| Tool        | Config                           | Command                          |
|------------|----------------------------------|----------------------------------|
| Checkstyle | `src/main/resources/checkstyle.xml` | `mvn checkstyle:check`        |
| SpotBugs   | built-in                         | `mvn spotbugs:check`             |
| PMD        | GitHub Actions                   | (CI only)                        |
| SonarQube  | `sonar-project.properties`       | `mvn sonar:sonar`                |
| Error Prone| profile: `error-prone`           | `mvn build -P error-prone`       |
| OpenRewrite| profile: `open-rewrite`          | `mvn rewrite:runNoFork`          |

SonarCloud: org=`juanpablojimenezesclusa`, project=`JuanPabloJimenezEsclusa_camila-services`, branch=`develop`

---

## Constraints

- **NO Lombok** — use records instead
- **SLF4J** via `@Slf4j` → `log.info()`, `log.error()`, etc.
- **All packages** require `package-info.java`
- **API-first**: OpenAPI specs in `src/main/resources/api/product.yml`, validated with Spectral
- **Pre-commit hooks**: trailing whitespace, YAML/JSON validation
- **Testing mandatory**: unit + integration + architecture tests required before PR
- **Performance**: JMeter tests in `camila-performance/`
- **No `.block()`** anywhere in reactive chains

---

## Troubleshooting

| Issue                        | Fix                                                                       |
|------------------------------|---------------------------------------------------------------------------|
| Java version error           | Set `JAVA_HOME` to JDK 25+                                                |
| Test timeouts                | `~/.testcontainers.properties` → `testcontainers.reuse.enable=true`       |
| SonarQube fails              | Set `SONAR_TOKEN` env var                                                 |
| Docker build fails           | Check daemon: `docker ps`; check disk: `df -h`                            |
| Consul issues (local tests)  | `unset SPRING_PROFILES_ACTIVE` or set `spring.cloud.consul.discovery.enabled=false` |
