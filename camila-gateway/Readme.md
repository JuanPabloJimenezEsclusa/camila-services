# camila-gateway

This project implements an API gateway for services, including support for patterns like `circuit-breaker` and `retry` to improve service resilience.

## Prerequisites

* JDK >= 21
* Docker >= 24.0.6
* Maven >= 3.8.8
* Spring >= 6.x
* Spring-boot >= 3.2.x

## Architecture

```txt
📦gateway
 ┣ 📂presentation
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

> You need to modify the `/etc/hosts` file to map `127.0.0.1  gateway  keycloak` and configure `keycloak` as a Single Sign-On (SSO) service.

```bash
curl --location 'http://gateway:8090/product-dev/api/products?salesUnits=0.80&stock=0.20&page=0&size=20' \
  --header 'Accept: application/json' \
  --header 'Authorization: Bearer ***'

curl --location 'http://gateway:8090/product-dev/api/products/1' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer ***'
```

## Operations (build, deploy)

For instructions on building and deploying this project, refer to the [Operate - Readme](.operate/Readme.md) file.
