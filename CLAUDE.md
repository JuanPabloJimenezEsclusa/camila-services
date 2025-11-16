# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Project Overview

**Camila Services** is an experimental microservices platform implementing product classification and ranking for camila.shopping. It uses a weighted algorithm considering metrics like units sold, stock ratio, profit margin, and days in stock to optimize product presentation.

- **Architecture Pattern**: Hexagonal Architecture with Domain-Driven Design (DDD)
- **Programming Paradigm**: Reactive (Project Reactor)
- **Language**: Java with Spring Boot 3.5.x
- **Build Systems**: Maven 3.9.x (primary) and Gradle 9.1.0 (experimental)
- **Deployment**: Docker + Kubernetes + AWS CloudFormation/Terraform

---

## Technology Stack

### Core Development
- **JDK**: 25.x (OpenJDK)
- **Spring Framework**: 6.x
- **Spring Boot**: 3.5.x
- **Spring Cloud**: 2025.0.x
- **GraalVM**: 25+37.1 (native compilation)

### Databases
- **MongoDB**: 8.x (default)
- **Couchbase**: 7.x (alternative)
- **Redis**: 8.x (caching)

### Testing (Comprehensive Strategy)
- **JUnit 5**: 6.0.0+ (upgraded from 5.11.4)
- **Cucumber**: 7.30.0+ (upgraded from 7.22.2, BDD testing)
- **Pitest**: 1.21.0+ (upgraded from 1.20.1, mutation testing)
- **Testcontainers**: 2.0.1+ (upgraded from 1.21.3, integration tests)
- **ArchUnit**: 1.4.1+ (architecture tests)
- **Spectral**: OpenAPI validation
- **ChaosMonkey**: 3.2.0+ (resilience testing)

### Code Quality
- **SonarQube**: SonarCloud integration (main organization: juanpablojimenezesclusa, project: JuanPabloJimenezEsclusa_camila-services)
- **Checkstyle**: Google Java Style Guide
- **SpotBugs**: Static bug detection
- **PMD**: Code analysis
- **Error Prone**: Compiler plugin for stricter checks
- **OpenRewrite**: Automated refactoring

### Deployment
- **Docker**: 28.3.3+
- **Kubernetes**: 1.34.0+
- **Knative**: 1.19.6+ (Serverless)
- **Terraform**: 1.13.0+

---

## Recent Dependency Updates (November 2025)

### Maven Dependency Upgrades
- **OpenTelemetry**: 1.54.1 → 1.55.0 (observability improvements)
- **gRPC**: 1.75.0 → 1.76.0 (enhanced RPC performance)
- **OKHttp**: 5.1.0 (HTTP client compatibility)
- **Dependency-Check-Maven**: 12.1.6 → 12.1.8 (security scanning)

### Gradle Build Tool Upgrades
- **JUnit Platform Suite**: 6.0.0+ (test framework)
- **Cucumber**: 7.20.1 → 7.30.0 (BDD framework)
- **Spring Boot**: 3.5.7 (stable release)
- **GraalVM Native Tools**: 0.11.2 (native compilation)
- **OpenAPI Generator**: 7.17.0 (API spec generation)

### Testing Framework Enhancements
- **Pitest**: 1.20.3 → 1.21.0 (mutation testing improvements)
- **Testcontainers**: 1.21.3 → 2.0.1 (major version upgrade with enhanced container support)

**Status**: All 178 tests passing (68 unit + 110 integration tests). Build validated with error-prone, quality-check, checkstyle, and spotbugs profiles.

---

## Architecture Overview

### Hexagonal Architecture Layers

Each microservice follows hexagonal (ports and adapters) pattern:

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

### Microservices

| Service | Port | Purpose |
|---------|------|---------|
| **camila-product-api** | 8080 | Core product ranking & search API |
| **camila-gateway** | 8090 | API Gateway with circuit breaker & retry |
| **camila-discovery** | 8761 | Service discovery (Eureka) |
| **camila-config** | 8888 | Spring Cloud Config Server |
| **camila-admin** | 9093 | Spring Boot Admin UI |

### camila-product-api Communication Protocols

Supports REST (primary), GraphQL, WebSocket, RSocket, and gRPC.

---

## Build System

### Maven (Primary)

**Key Files**:
- `pom.xml`: Parent POM with 6 modules, Spring Boot 3.5.6, dependency management for 60+ plugins
- Configuration in: `src/main/resources/checkstyle.xml`, `sonar-project.properties`

**Build Commands**:
```bash
mvn clean build                              # Standard build
mvn clean package -DskipTests=true           # Fast build (no tests)
mvn clean verify                             # Build + unit + integration tests
mvn clean test                               # Unit tests only
mvn test -Dtest=ProductControllerTest        # Single test class
mvn spotbugs:check                           # Bug analysis
mvn checkstyle:check                         # Style checking
mvn -B build -P error-prone,quality-check    # Strict error checking
mvn spring-boot:build-image                  # Docker build via Buildpacks
mvn rewrite:runNoFork -Popen-rewrite         # Automated refactoring
mvn versions:display-dependency-updates      # Check outdated dependencies
```

**Profiles**:
- `error-prone`: Stricter compiler flags
- `quality-check`: All code analysis tools
- `pitest`: Mutation testing
- `open-rewrite`: Automated code refactoring

### Gradle (Experimental)

**Key Files**:
- `build.gradle`: Root script with 13 plugins
- `settings.gradle`: Module declarations
- `gradle.properties`: JVM options, build cache settings

**Build Commands**:
```bash
gradle build                                 # Standard build
gradle clean build                           # Clean build
gradle unitTest                              # Unit tests only
gradle clean check                           # Full verification
gradle qualityBuild -PerrorProne             # With error-prone
gradle aggregateJacoco                       # Code coverage
```

---

## Testing Strategy

### Test Organization

**Test Location**: `src/test/java/` following domain structure

**Test Types**:

1. **Unit Tests** (`*Test.java`):
   - JUnit 5, Mockito
   - Run: `mvn clean test`

2. **Integration Tests** (`*ITCase.java`):
   - Testcontainers (MongoDB, Couchbase)
   - Run: `mvn clean verify`
   - Configuration: `.testcontainers.properties` (reuse enabled)

3. **Architecture Tests** (`src/test/java/api/architecture/`):
   - ArchUnit validating hexagonal patterns
   - Run: `mvn clean test`

4. **Behavior Tests** (`*BehaviourRunner.java`):
   - Cucumber scenarios
   - Run: `mvn test -Dtest=ProductBehaviourRunner`

5. **Mutation Tests** (via Pitest):
   - Run: `mvn clean test -P pitest`
   - Reports: `target/pit-reports/`

6. **Contract Tests**:
   - Spectral (OpenAPI validation)
   - See: `camila-product-api/src/test/Readme.md`

### Code Coverage

- **Tool**: JaCoCo
- **Aggregation Module**: `camila-coverage-jacoco`
- **Exclusions**: Generated code, DTOs, configs, mappers
- **Report**: `target/site/jacoco/index.html`

### Running Tests Locally

```bash
mvn clean verify                                  # All tests
mvn clean test                                    # Unit only
mvn clean verify -P pitest                        # With mutations
mvn clean verify site -P error-prone,quality-check # Full analysis
unset SPRING_PROFILES_ACTIVE                      # Disable Eureka for local tests
```

---

## Linting & Code Quality

### Tools Configuration

| Tool | Config Location | Command |
|------|-----------------|---------|
| **Checkstyle** | `src/main/resources/checkstyle.xml` | `mvn checkstyle:check` |
| **SpotBugs** | Built-in | `mvn spotbugs:check` |
| **PMD** | GitHub Actions only | CI/CD workflow |
| **SonarQube** | `sonar-project.properties` | `mvn sonar:sonar` |
| **Error Prone** | Profile: `error-prone` | `mvn build -P error-prone` |
| **OpenRewrite** | Profile: `open-rewrite` | `mvn rewrite:runNoFork` |

### Code Style

**EditorConfig** (`.editorconfig`):
- Charset: UTF-8, Line ending: LF
- Indent: 2 spaces, Max line: 100 characters

**Checkstyle**: Google Java Style Guide

### SonarQube Configuration

- **Host**: https://sonarcloud.io
- **Organization**: juanpablojimenezesclusa
- **Project**: JuanPabloJimenezEsclusa_camila-services
- **Branch**: develop
- **Excluded**: Generated code, configs, DTOs, mappers

---

## CI/CD Configuration

### GitHub Workflows (`.github/workflows/`)

| Workflow | Trigger | Purpose |
|----------|---------|---------|
| **build-deploy-maven.yml** | Push to `main` | Compile, test, deploy to GitHub Packages |
| **code-analysis-pmd.yml** | PR/push to `develop` | PMD analysis per module |
| **dependency-review.yml** | Pull requests | CVE vulnerability check |
| **docker-publish.yml** | Push to `main`/PR | Build & push Docker images |
| **sonarqube.yml** | PR/push to `develop` | SonarCloud analysis |
| **test-behaviour.yml** | PR/push to `develop` | Cucumber scenario testing |
| **test-mutations.yml** | PR/push to `develop` | Pitest mutation analysis |

### CI/CD Features

- Concurrency control (cancels in-progress jobs)
- Maven/dependency caching for speed
- Testcontainers reuse enabled
- GPG signing for secure deployments
- Parallel module testing

---

## Key Configuration Files

### Spring Boot Configuration

**File**: `src/main/resources/application.yml` (per service)

**Key Properties**:
- Virtual threads enabled (Java 19+)
- WebFlux reactive stack
- Database switching (MongoDB/Couchbase profiles)
- OAuth2 + Keycloak security
- Distributed caching (Redis) + local (Caffeine)
- Observability (Zipkin, Prometheus)

**Profiles**:
- `loc|dev`: Local development with ChaosMonkey
- `dev`: Docker Compose with Eureka/Config
- `int`: Integration/K8s
- `pre|pro`: Production with OAuth2
- `local-compose`: Docker Compose with Dev Services

### OpenAPI Specification

**File**: `src/main/resources/api/product.yml`

**Linting**: `.spectral.yml` (Stoplight rules, custom email/server/operation validations)

### Docker Compose Environment

**Location**: `camila-orchestrator/dev/compose/`

**Services Included**:
- Product API, Gateway, Discovery, Config, Admin
- MongoDB, Couchbase, Redis
- Prometheus, Grafana, ELK stack
- PostgreSQL (for config persistence)

**Commands**:
```bash
cd camila-orchestrator/dev/compose
./start.sh           # Start environment
./stop.sh            # Stop environment
```

---

## Directory Structure

```
camila-services/
├── camila-product-api/              # Core microservice
│   ├── src/main/java/com/camila/api/product/
│   │   ├── domain/                  # Domain logic, ports, services
│   │   ├── application/             # Use cases (services)
│   │   └── infrastructure/          # Adapters
│   │       ├── adapter/input/       # REST, GraphQL, gRPC, WebSocket, RSocket, Security
│   │       └── adapter/output/      # Persistence (MongoDB, Couchbase), Cache
│   ├── src/test/                    # Unit, integration, architecture tests
│   └── src/main/resources/          # API specs, configuration
│
├── camila-discovery/                # Eureka service discovery
├── camila-gateway/                  # API Gateway with resilience patterns
├── camila-config/                   # Spring Cloud Config Server
├── camila-admin/                    # Spring Boot Admin UI
├── camila-coverage-jacoco/          # Code coverage aggregation
│
├── camila-orchestrator/
│   ├── dev/compose/                 # Local Docker Compose environment
│   ├── int/k8s/                     # Kubernetes (Kind, AWS EKS)
│   └── pre/aws/                     # Production (CloudFormation, Terraform)
│
├── camila-performance/              # JMeter performance tests
│
├── .github/workflows/               # CI/CD pipelines
├── src/main/resources/              # Checkstyle, changelog template
├── pom.xml                          # Maven parent POM
├── build.gradle                     # Gradle root script
├── settings.gradle                  # Gradle modules
├── gradle.properties                # Gradle config
│
├── Readme.md                        # Project main documentation
├── CONTRIBUTING.md                  # Contribution guidelines
├── CODE_OF_CONDUCT.md               # Community standards
├── CHANGELOG.md                     # Auto-generated version history
└── LICENSE.md                       # GNU GPL v3 license
```

---

## Development Workflow

### Branch Strategy

- **Main**: `main` (production-ready, tagged releases)
- **Development**: `develop` (integration/testing)
- **Feature**: `feature/*` from `develop`

### Git Workflow

1. Create feature branch from `develop`
2. Commit with present tense, imperative mood (e.g., "Add feature", "Fix bug")
3. Push to remote
4. Create PR to `develop`
5. CI checks must pass
6. Code review required
7. Merge to `develop`

### Essential Commands

```bash
# Initial setup
mvn clean build                                           # First build
cd camila-orchestrator/dev/compose && ./start.sh          # Local environment

# Development
mvn clean verify                                          # Run all tests
mvn test -Dtest=ProductControllerTest                    # Single test
mvn spotbugs:check && mvn checkstyle:check               # Quality checks

# Before PR submission
mvn clean verify site -P error-prone,quality-check        # Full analysis
mvn spring-boot:build-image -Dmaven.test.skip=true       # Build images

# Automated improvements
mvn rewrite:runNoFork -Popen-rewrite                     # Code refactoring
mvn versions:display-dependency-updates                  # Check updates

# Local debugging
export SPRING_PROFILES_ACTIVE=local-compose              # Use Docker Compose config
mvn spring-boot:run -pl camila-product-api               # Run single service
```

---

## Local Development Setup

### Prerequisites

- JDK 25+ (verify: `java -version`)
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
- Config Server: http://localhost:8888/
- Admin Dashboard: http://localhost:9093/

**Databases**:
- MongoDB: localhost:27017
- Couchbase: localhost:8091
- Redis: localhost:6379

### Troubleshooting

| Issue | Solution |
|-------|----------|
| Build fails with Java version error | Ensure JDK 25+: `java -version` and set `JAVA_HOME` |
| Tests timeout | Enable testcontainers reuse: `~/.testcontainers.properties` with `testcontainers.reuse.enable=true` |
| SonarQube analysis fails | Set `SONAR_TOKEN` environment variable |
| Docker build fails | Check daemon: `docker ps`, verify disk space: `df -h` |

---

## Important Notes for Future Development

1. **Hexagonal Architecture**: Always keep domain logic separate from adapters. Input adapters (REST, GraphQL) must not contain business logic; output adapters (database, cache) must not leak domain implementation details.

2. **Reactive Programming**: All I/O operations use Project Reactor (Mono/Flux). No blocking calls in reactive chains.

3. **Testing Priority**: This project prioritizes comprehensive testing (unit, integration, architecture, behavior, mutation). All new code requires tests before PR submission.

4. **Database Flexibility**: Support both MongoDB and Couchbase via adapter pattern. Configuration switches between implementations via profiles.

5. **Deployment Readiness**: Code must build Docker images cleanly via buildpacks and deploy to both local Compose and Kubernetes environments.

6. **Code Quality Gate**: SonarQube integration on `develop` branch. PRs must pass analysis before merge.

7. **Performance Testing**: JMeter tests available. Use for critical API paths before production deployment.

8. **API Specification First**: OpenAPI specs in `src/main/resources/api/product.yml` are validated via Spectral. Keep in sync with implementation.

---

## Quick Reference Commands

```bash
# Build & Test
mvn clean build                                     # Standard build
mvn clean verify                                    # All tests
mvn clean verify -P pitest                          # With mutation tests

# Code Quality
mvn clean verify site -P error-prone,quality-check  # Full analysis
mvn sonar:sonar                                     # SonarQube analysis
mvn checkstyle:check && mvn spotbugs:check          # Style + bugs

# Development
mvn spring-boot:run -pl camila-product-api          # Run service
mvn spring-boot:build-image                         # Docker build
mvn rewrite:runNoFork -Popen-rewrite                # Auto-refactor

# Local Environment
cd camila-orchestrator/dev/compose && ./start.sh    # Start services
cd camila-orchestrator/dev/compose && ./stop.sh     # Stop services

# Single Test
mvn test -Dtest=ProductControllerTest
```
