# camila-gateway

This project implements an API gateway for services, including support for patterns like
`circuit-breaker` and `retry` to improve service resilience.

## Prerequisites

* JDK ~= [24.x](https://openjdk.org/projects/jdk/24/)
* Docker ~= [28.x](https://docs.docker.com/engine/release-notes/28/)
* Maven ~= [3.9.x](https://maven.apache.org/download.cgi)
* Gradle ~= [9.0.0](https://gradle.org/releases/#9.0.0)
* Spring ~= [6.x](https://spring.io/projects/spring-framework#learn)
* Spring-boot ~= [3.5.x](https://spring.io/projects/spring-boot#learn)
* Spring-cloud ~= [2025.0.x](https://spring.io/projects/spring-cloud#learn)

## Architecture

```txt
📦gateway
 ┃ ┗ 📂infrastructure
 ┃   ┗ 📂adapter
 ┃     ┗ 📂input
 ┃       ┗ 📂rest
 ┗ 📜CamilaGatewayApplication.java
```

## Links

* API DOC (dev): <http://localhost:8090/swagger-ui.html>
* Actuator:
  * <http://localhost:8090/actuator/health>
  * <http://localhost:8090/actuator/metrics/spring.cloud.gateway.requests>

## API Request Examples

```bash
curl -X 'GET' \
  'http://localhost:8090/product-dev/api/products?salesUnits=0.80&stock=0.20&page=0&size=20' \
  -H 'accept: application/json'
  
curl -X 'GET' \
  'http://localhost:8090/product-dev/api/products/1' \
  -H 'accept: application/json'
```

## Examples with Security: `oauth2` delegated to an `SSO` service

> You need to modify the `/etc/hosts` file to map `127.0.0.1  gateway  keycloak` and configure
`keycloak` as a Single Sign-On (SSO) service.

```bash
curl --location 'http://gateway:8090/product-dev/api/products?salesUnits=0.80&stock=0.20&page=0&size=20' \
  --header 'Accept: application/json' \
  --header 'Authorization: Bearer ***'

curl --location 'http://gateway:8090/product-dev/api/products/1' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer ***'
```

## Operations (build, deploy)

For instructions on building and deploying this project, refer to
the [Operate - Readme](.operate/Readme.md) file.
