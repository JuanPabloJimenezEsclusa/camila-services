---
title: Home
nav_order: 1
description: "Camila Services — Product classification & ranking microservices platform"
permalink: /
---

![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=JuanPabloJimenezEsclusa_camila-services&metric=alert_status)
![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=JuanPabloJimenezEsclusa_camila-services&metric=sqale_rating)
![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=JuanPabloJimenezEsclusa_camila-services&metric=reliability_rating)
![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=JuanPabloJimenezEsclusa_camila-services&metric=security_rating)
![Coverage](https://sonarcloud.io/api/project_badges/measure?project=JuanPabloJimenezEsclusa_camila-services&metric=coverage)
![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=JuanPabloJimenezEsclusa_camila-services&metric=ncloc)

[![Build Deploy Maven](https://github.com/JuanPabloJimenezEsclusa/camila-services/actions/workflows/build-deploy-maven.yml/badge.svg)](https://github.com/JuanPabloJimenezEsclusa/camila-services/actions/workflows/build-deploy-maven.yml)
[![Docker Publish](https://github.com/JuanPabloJimenezEsclusa/camila-services/actions/workflows/docker-publish.yml/badge.svg)](https://github.com/JuanPabloJimenezEsclusa/camila-services/actions/workflows/docker-publish.yml)
[![SonarQube](https://github.com/JuanPabloJimenezEsclusa/camila-services/actions/workflows/sonarqube.yml/badge.svg)](https://github.com/JuanPabloJimenezEsclusa/camila-services/actions/workflows/sonarqube.yml)

---

## Summary

The **Camila Services** project implements a product classification and ranking system for camila.shopping. Using a weighted algorithm that considers metrics like units sold and stock ratio, it optimizes product presentation to enhance customer search experience.

Built with modern technologies, hexagonal architecture, and reactive programming, the project follows rigorous testing and deployment best practices with a strong emphasis on code quality.

{: .highlight }
> **Experimental Nature** — This project serves as a comprehensive technical laboratory to explore cutting-edge concepts including reactive programming, hexagonal architecture, Buildpack Docker images, and extensive quality gates.

---

## Services

| Service              | Port | Purpose                       |
|----------------------|------|-------------------------------|
| `camila-product-api` | 8080 | Product ranking & search API  |
| `camila-gateway`     | 8090 | API Gateway (circuit breaker) |
| `camila-admin`       | 8100 | Spring Boot Admin UI          |

**Protocols:** REST (primary), GraphQL, WebSocket, RSocket, gRPC

---

## Architecture

Built on **Hexagonal Architecture** (Ports & Adapters) with **Domain-Driven Design**.

```
                    ┌─────────────────────────────────┐
                    │        Domain Layer             │
                    │  Models, Ports, Use Cases       │
                    └────────┬───────────────┬────────┘
                        ▲                   ▲
              ┌─────────┴─────────┐  ┌──────┴───────┐
              │  Input Adapters   │  │Output Adapters│
              │  REST, GraphQL,   │  │Mongo, Cache   │
              │  gRPC, WebSocket  │  │Couchbase      │
              └───────────────────┘  └──────────────┘
```

### Key Principles
- Domain layer has **zero framework dependencies**
- All I/O via reactive types (`Mono`/`Flux`)
- Port interfaces in domain, implementations in infrastructure

[View detailed architecture diagrams →](https://github.com/JuanPabloJimenezEsclusa/camila-services/tree/develop/.docs/architecture)

---

## Technology Stack

| Category          | Technology                              |
|-------------------|-----------------------------------------|
| Language          | Java 25                                 |
| Framework         | Spring Boot 4.0.x                       |
| Paradigm          | Reactive (Project Reactor)              |
| Build             | Maven 3.9.x / Gradle 9.5.0              |
| Databases         | MongoDB, Couchbase                      |
| Cache             | Redis (distributed) + Caffeine (local)  |
| Security          | OAuth2 + Keycloak                       |
| Observability     | Zipkin, Prometheus, Consul              |
| Testing           | JUnit 5, Testcontainers, ArchUnit, Cucumber |

---

## Quick Start

### Prerequisites
- JDK 25+
- Maven 3.9.x
- Docker 29+

### Build & Run
```bash
# Build all services
mvn clean build

# Run product API locally
mvn spring-boot:run -pl camila-product-api

# Start full environment
cd camila-orchestrator/dev/compose && ./start.sh
```

### Access Points (local)

| Service      | URL                                                      |
|-------------|----------------------------------------------------------|
| Product API  | `http://localhost:8080/product-dev/api/swagger-ui.html`  |
| Gateway      | `http://localhost:8090/swagger-ui.html`                  |
| Consul       | `http://localhost:8500/ui/`                              |
| Admin        | `http://localhost:8100/`                                 |

---

## Documentation

- [API Reference](https://github.com/JuanPabloJimenezEsclusa/camila-services/tree/develop/.docs/api)
- [Contributing Guide](https://github.com/JuanPabloJimenezEsclusa/camila-services/blob/develop/CONTRIBUTING.md)
- [Code of Conduct](https://github.com/JuanPabloJimenezEsclusa/camila-services/blob/develop/CODE_OF_CONDUCT.md)
- [Changelog](https://github.com/JuanPabloJimenezEsclusa/camila-services/blob/develop/CHANGELOG.md)
- [License (MIT)](https://github.com/JuanPabloJimenezEsclusa/camila-services/blob/develop/LICENSE.md)

---

## License

Camila Services is open-source software licensed under the MIT License.
