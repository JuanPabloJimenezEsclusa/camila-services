# camila-gateway

Contiene un gateway para servicios (incluye los patrones: `circuit-breaker` y `retry`)

## Pre-condiciones

* JDK >= 21
* Docker >= 24.0.6
* maven >= 3.8.8
* Spring >= 6.x
* Spring-boot >= 3.2.x

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

## Ejemplos con seguridad: `oauth2` delegada a un servicio: `SSO` 

> Es necesario configurar el archivo: `/etc/hosts` para enlazar: `127.0.0.1  gateway  keycloak` y configurar `keycloak` como servicio: single sing-on (SSO)

```bash
curl --location 'http://gateway:8090/product-dev/api/products?salesUnits=0.80&stock=0.20&page=0&size=20' \
  --header 'Accept: application/json' \
  --header 'Authorization: Bearer ***'

curl --location 'http://gateway:8090/product-dev/api/products/1' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer ***'
```

## Operaciones (build, deploy)

[Operar - Readme](.operate/Readme.md)
