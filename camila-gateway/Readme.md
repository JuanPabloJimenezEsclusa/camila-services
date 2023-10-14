# camila-gateway

Contiene un gateway para servicios (incluye los patrones: `circuit-breaker` y `retry`)

## Pre-condiciones

* JDK >= 17
* Docker >= 24.0.6
* maven >= 3.9.4
* GraalVM >= 22.3.2
* Spring >= 6.x
* Spring-boot >= 3.1.x

## Arquitectura

```txt
📦gateway
 ┣ 📂presentation
 ┗ 📜CamilaGatewayApplication.java
```

## Enlaces

* API DOC (dev): <http://localhost:8090/swagger-ui.html>
* Actuator:
  * <http://localhost:8090/actuator/health>
  * <http://localhost:8090/actuator/metrics/spring.cloud.gateway.requests>

## Ejemplos de petición API

```bash
curl -X 'GET' \
  'http://localhost:8090/product-dev/api/products?salesUnits=0.80&stock=0.20&page=0&size=20' \
  -H 'accept: application/json'
  
curl -X 'GET' \
  'http://localhost:8090/product-dev/api/products/1' \
  -H 'accept: application/json'
```

## Operaciones (build, deploy)

[Operar - Readme](.operate/Readme.md)
