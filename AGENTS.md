# AGENTS.md

This document provides essential guidance for AI coding agents working in the Camila Services codebase.

---

## Build & Test Commands

### Maven (Primary Build System)

```bash
# Build & compile
mvn clean build                                     # Standard build
mvn clean package -DskipTests=true                  # Fast build without tests
mvn clean verify                                    # Full build with all tests
mvn -B build -P error-prone,quality-check           # Strict build with all checks

# Testing
mvn clean test                                      # Unit tests only (*Test.java)
mvn clean verify                                    # Unit + integration tests (*ITCase.java)
mvn test -Dtest=ProductRestAdapterUnitTest          # Single test class
mvn test -Dtest=ProductRestAdapterUnitTest#shouldFindProductById  # Single test method
mvn clean verify -P pitest                          # Mutation testing
mvn test -Dtest=ProductBehaviourRunner              # Cucumber BDD tests

# Code quality
mvn checkstyle:check                                # Google Java Style validation
mvn spotbugs:check                                  # Static analysis
mvn clean verify site -P error-prone,quality-check  # Full quality analysis
mvn sonar:sonar                                     # SonarCloud analysis

# Dependency management
mvn versions:display-dependency-updates             # Check outdated dependencies
mvn rewrite:runNoFork -Popen-rewrite                # Automated refactoring

# Docker
mvn spring-boot:build-image                         # Build Docker image via Buildpacks

# Development
mvn spring-boot:run -pl camila-product-api          # Run single service locally
```

### Gradle (Experimental)

```bash
gradle build                                        # Standard build
gradle clean build                                  # Clean build
gradle unitTest                                     # Unit tests only
gradle clean check                                  # Full verification
gradle qualityBuild -PerrorProne                    # With error-prone
gradle aggregateJacoco                              # Code coverage
```

---

## Project Overview

**Camila Services** is a microservices platform implementing product classification and ranking with a weighted algorithm.

- **Architecture**: Hexagonal Architecture with Domain-Driven Design (DDD)
- **Paradigm**: Reactive Programming (Project Reactor)
- **Language**: Java 25 with Spring Boot 3.5.x
- **Build**: Maven 3.9.x (primary), Gradle 9.1.0 (experimental)

### Microservices

| Service | Port | Purpose |
|---------|------|---------|
| **camila-product-api** | 8080 | Core product ranking & search API |
| **camila-gateway** | 8090 | API Gateway with circuit breaker & retry |
| **camila-discovery** | 8761 | Service discovery (Eureka) |
| **camila-config** | 8888 | Spring Cloud Config Server |
| **camila-admin** | 9093 | Spring Boot Admin UI |

### Communication Protocols

camila-product-api supports: REST (primary), GraphQL, WebSocket, RSocket, and gRPC.

---

## Code Style Guidelines

### Formatting & Structure

- **Indentation**: 2 spaces (NO tabs)
- **Line length**: Max 140 characters (checkstyle), but aim for 120 (editorconfig)
- **Encoding**: UTF-8
- **Line endings**: LF (Unix-style)
- **Final newline**: Not required
- **Braces**: K&R style (opening brace on same line)
- **Empty blocks**: Use `{}` with no spaces

### Imports

- **Order**: Static imports, Java standard library, third-party packages
- **Grouping**: Separate groups with blank lines
- **No wildcards**: No star imports (`import java.util.*`)
- **Sort**: Alphabetically within groups

Example:
```java
import static org.mockito.Mockito.verify;

import java.util.Map;

import com.camila.api.product.domain.model.Product;
import org.springframework.stereotype.Service;
```

### Naming Conventions

- **Packages**: All lowercase, no underscores (`com.camila.api.product`)
- **Classes**: PascalCase (`ProductRestAdapter`, `ProductUseCase`)
- **Interfaces**: PascalCase, no 'I' prefix (`ProductRepository`)
- **Methods**: camelCase (`findByInternalId`, `sortByMetricsWeights`)
- **Variables**: camelCase (`productUseCase`, `internalId`)
- **Constants**: UPPER_SNAKE_CASE (`SORTED_REQUEST_PARAMS`)
- **Type parameters**: Single uppercase letter or PascalCase ending in 'T' (`T`, `ProductT`)
- **Test methods**: Snake_case allowed in test names for readability
- **Allowed abbreviations**: DTO, API, OAUTH, ITC (max 3 chars)

### Types & Declarations

- **Use records**: For immutable data classes (domain models, DTOs)
- **Use final**: For parameters and local variables when possible
- **No raw types**: Always use generics (`List<Product>` not `List`)
- **Reactive types**: Use `Mono<T>` for single values, `Flux<T>` for streams
- **Java 25 features**: Virtual threads enabled, pattern matching, sealed classes allowed

Example:
```java
public record Product(
  String id,
  String internalId,
  String name,
  String category,
  int salesUnits,
  Map<String, Integer> stock,
  double profitMargin,
  int daysInStock
) {}
```

### Error Handling

- **Reactive errors**: Use `.onErrorMap()`, `.onErrorResume()`, not try-catch
- **Custom exceptions**: Extend from domain-specific base exceptions
- **Exception hierarchy**: `ProductException` → `NotFoundException`
- **Never catch generic Exception**: Catch specific exceptions
- **Logging**: Log errors at appropriate levels (error, warn, info, debug)

Example:
```java
return productRepository.findByInternalId(id)
  .switchIfEmpty(Mono.error(new NotFoundException("Product not found: " + id)))
  .onErrorMap(DatabaseException.class, e -> new ProductException("Database error", e));
```

---

## Architecture Patterns

### Hexagonal Architecture (Ports & Adapters)

```
┌─────────────────────────────────────────┐
│        Domain Layer                      │
│  Services, Models, Ports, Use Cases      │
└─────────────────────────────────────────┘
            ▲                    ▲
      ┌─────┴─────────┐   ┌─────┴──────┐
      │Input Adapters │   │Output Adapters│
      │(REST, GraphQL,│   │(Mongo, Cache) │
      │gRPC, WebSocket)   │               │
      └────────────────┘   └──────────────┘
```

**Layer Structure**:
- **Domain layer**: Business logic, models, ports (interfaces)
  - Location: `camila-product-api-domain/`
  - No dependencies on infrastructure
- **Application layer**: Use cases, orchestration
  - Location: `camila-product-api-application/`
- **Infrastructure layer**: Adapters (input/output), configuration
  - Input adapters: `infrastructure/driving/` (REST, GraphQL, gRPC, WebSocket, RSocket)
  - Output adapters: `infrastructure/driven/` (MongoDB, Couchbase, Cache)

### Key Principles

1. **Domain independence**: Domain code must not import Spring, database, or framework classes
2. **Dependency inversion**: Infrastructure depends on domain, not vice versa
3. **Port interfaces**: Define contracts in domain, implement in infrastructure
4. **Reactive programming**: All I/O operations use Project Reactor (Mono/Flux)
5. **No blocking calls**: Never use `.block()` in reactive chains

---

## Testing Standards

### Test Organization

**Test Types**:

1. **Unit Tests** (`*UnitTest.java`): JUnit 5, Mockito - `mvn clean test`
2. **Integration Tests** (`*ITCase.java`): Testcontainers (MongoDB, Couchbase) - `mvn clean verify`
3. **Architecture Tests** (`*ArchitectureTest.java`): ArchUnit validating hexagonal patterns
4. **BDD Tests** (`*BehaviourRunner.java`): Cucumber scenarios
5. **Mutation Tests**: Pitest - `mvn clean verify -P pitest`
6. **Contract Tests**: Spectral (OpenAPI validation)

### Test Structure

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("[UT][ProductRestAdapter] Product REST Adapter Unit Tests")
class ProductRestAdapterUnitTest {

  @Mock
  private ProductUseCase productUseCase;

  @InjectMocks
  private ProductRestAdapter productRestAdapter;

  @Test
  @DisplayName("Should find product by internal ID")
  void shouldFindProductById() {
    // Given
    final var internalId = "123";
    final var product = Instancio.of(Product.class).create();
    when(productUseCase.findByInternalId(internalId)).thenReturn(Mono.just(product));

    // When & Then
    StepVerifier.create(productRestAdapter.findById(internalId, "", "", null))
      .expectNext(expectedDTO)
      .verifyComplete();

    verify(productUseCase).findByInternalId(internalId);
  }
}
```

### Test Best Practices

- **Use StepVerifier**: For testing reactive streams
- **Use Instancio**: For test data generation (avoid manual object creation)
- **Given-When-Then**: Structure all tests with this pattern
- **@DisplayName**: Provide clear, human-readable test descriptions
- **Testcontainers**: For integration tests with MongoDB, Couchbase, Redis
- **Verify mocks**: Always verify expected interactions
- **Testcontainers reuse**: Enable in `~/.testcontainers.properties` with `testcontainers.reuse.enable=true`

### Code Coverage

- **Tool**: JaCoCo
- **Aggregation Module**: `camila-coverage-jacoco`
- **Exclusions**: Generated code, DTOs, configs, mappers
- **Report**: `target/site/jacoco/index.html`

---

## Documentation

### Javadoc Requirements

- **Public classes**: Must have class-level Javadoc
- **Public methods**: Must have method-level Javadoc (except @Override, @Test)
- **Protected methods**: Should have Javadoc
- **Parameters**: Use `@param` tags
- **Return values**: Use `@return` tags
- **Exceptions**: Use `@throws` tags
- **Format**: First sentence is summary, followed by detailed description

Example:
```java
/**
 * Retrieves a product by its internal identifier.
 *
 * @param internalId the internal product identifier
 * @return a Mono containing the product if found
 * @throws NotFoundException if the product does not exist
 */
Mono<Product> findByInternalId(String internalId);
```

---

## Configuration & Profiles

### Spring Boot Profiles

- `loc|dev`: Local development with ChaosMonkey
- `dev`: Docker Compose with Eureka/Config
- `int`: Integration/K8s
- `pre|pro`: Production with OAuth2
- `local-compose`: Docker Compose with Dev Services

### Key Configuration

**Virtual threads**: Enabled (Java 19+)
**Security**: OAuth2 + Keycloak
**Caching**: Redis (distributed) + Caffeine (local)
**Observability**: Zipkin, Prometheus

### Database Switching

Support for both MongoDB and Couchbase via profiles:
- Set `repository.technology=mongodb` or `repository.technology=couchbase`
- Adapters implement the same port interface

---

## Local Development

### Prerequisites

- JDK 25+ (`java -version`)
- Maven 3.9.x or Gradle 9.1.0
- Docker 28.3.3+
- Docker Compose 2.35.0+

### Starting Local Environment

```bash
cd camila-orchestrator/dev/compose
./start.sh
```

**Access Points**:
- Product API: http://localhost:8080/product-dev/api/swagger-ui.html
- Gateway: http://localhost:8090/swagger-ui.html
- Service Discovery: http://localhost:8761/
- Admin Dashboard: http://localhost:9093/

**Databases**:
- MongoDB: localhost:27017
- Couchbase: localhost:8091
- Redis: localhost:6379

---

## Git Commit Guidelines

- **Present tense**: "Add feature" not "Added feature"
- **Imperative mood**: "Move cursor to..." not "Moves cursor to..."
- **First line**: Max 72 characters
- **Types**: add, update, fix, refactor, test, docs, chore
- **Example**: `fix: resolve null pointer in ProductRestAdapter`

---

## Code Quality Tools

| Tool | Config Location | Command |
|------|-----------------|---------|
| **Checkstyle** | `src/main/resources/checkstyle.xml` | `mvn checkstyle:check` |
| **SpotBugs** | Built-in | `mvn spotbugs:check` |
| **PMD** | GitHub Actions only | CI/CD workflow |
| **SonarQube** | `sonar-project.properties` | `mvn sonar:sonar` |
| **Error Prone** | Profile: `error-prone` | `mvn build -P error-prone` |
| **OpenRewrite** | Profile: `open-rewrite` | `mvn rewrite:runNoFork` |

### SonarQube Configuration

- **Host**: https://sonarcloud.io
- **Organization**: juanpablojimenezesclusa
- **Project**: JuanPabloJimenezEsclusa_camila-services
- **Branch**: develop

---

## Important Notes

- **NO Lombok**: Project doesn't use Lombok; use Java records instead
- **SLF4J logging**: Use `@Slf4j` annotation and `log.info()`, `log.error()`
- **Package-info.java**: All packages should have package-info.java files
- **Database flexibility**: Support both MongoDB and Couchbase via profiles
- **Pre-commit hooks**: Enabled for trailing whitespace, YAML/JSON validation
- **API Specification First**: OpenAPI specs in `src/main/resources/api/product.yml` validated via Spectral
- **Testing Priority**: All new code requires comprehensive tests (unit, integration, architecture) before PR
- **Performance Testing**: JMeter tests available in `camila-performance/`

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Build fails with Java version error | Ensure JDK 25+: `java -version` and set `JAVA_HOME` |
| Tests timeout | Enable testcontainers reuse: `~/.testcontainers.properties` with `testcontainers.reuse.enable=true` |
| SonarQube analysis fails | Set `SONAR_TOKEN` environment variable |
| Docker build fails | Check daemon: `docker ps`, verify disk space: `df -h` |
| Eureka connection issues | Run: `unset SPRING_PROFILES_ACTIVE` for local tests |

---

## Quick Reference

```bash
# Before PR submission
mvn clean verify site -P error-prone,quality-check  # Full analysis
mvn checkstyle:check && mvn spotbugs:check          # Style + bugs

# Single service development
export SPRING_PROFILES_ACTIVE=local-compose
mvn spring-boot:run -pl camila-product-api

# Automated improvements
mvn rewrite:runNoFork -Popen-rewrite                # Auto-refactor
mvn versions:display-dependency-updates             # Check updates
```
