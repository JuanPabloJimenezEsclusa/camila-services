# gateway

This project implements an API gateway for services, including support for patterns like
`circuit-breaker` and `retry` to improve service resilience.

## Prerequisites

---

<details>
<summary><strong>Expand Prerequisites</strong></summary>

* JDK ~= [25.x](https://openjdk.org/projects/jdk/25/)
* Docker ~= [29.x](https://docs.docker.com/engine/release-notes/29/)
* Maven ~= [3.9.x](https://maven.apache.org/download.cgi)
* Gradle ~= [9.5.x](https://gradle.org/releases)
* Spring ~= [7.x](https://spring.io/projects/spring-framework#learn)
* Spring-boot ~= [4.0.x](https://spring.io/projects/spring-boot#learn)
* Spring-cloud ~= [2025.1.x](https://spring.io/projects/spring-cloud#learn)
* Native Image compilation
  * GraalVM ~= [25.x](https://www.graalvm.org/release-notes/JDK_25/)
  * GCC >= (linux, x86_64, 11.4.0)
    * `zlib1g-dev`

</details>

## Architecture

---

<details>
<summary><strong>Expand Architecture</strong></summary>

```txt
📦gateway
 ┃ ┗ 📂infrastructure
 ┃   ┗ 📂adapter
 ┃     ┗ 📂input
 ┃       ┗ 📂rest
 ┗ 📜CamilaGatewayApplication.java
```

</details>

## Links

* API DOC (dev): <http://localhost:8090/swagger-ui.html>
* Actuator:
  * <http://localhost:8090/actuator/health>
  * <http://localhost:8090/actuator/metrics/spring.cloud.gateway.requests>

## API Request Examples

---

<details>
<summary><strong>Expand API Request Examples</strong></summary>

```bash
curl -X 'GET' \
  'http://localhost:8090/product-dev/api/products?salesUnits=0.80&stock=0.20&page=0&size=20' \
  -H 'accept: application/json'
  
curl -X 'GET' \
  'http://localhost:8090/product-dev/api/products/1' \
  -H 'accept: application/json'
```

### Examples with Security: `oauth2` delegated to an `SSO` service

> You need to modify the `/etc/hosts` file to map `127.0.0.1  gateway  keycloak` and configure
`keycloak` as a Single Sign-On (SSO) service.

```bash
# Rest
curl --sLf 'http://gateway:8090/product-dev/api/products/1' \
  --header 'Accept: application/json' \
  --header "Authorization: Bearer ${TOKEN}" | jq .

curl -sLf 'http://gateway:8090/product-dev/api/products?salesUnits=0.80&stock=0.20&page=0&size=20' \
  --header 'Accept: application/json' \
  --header "Authorization: Bearer ${TOKEN}" | jq .

# Graphql
curl -sLf 'http://gateway:8090/product-dev/api/graphql' \
  --header 'Accept: application/json' \
  --header 'Content-Type: application/json' \
  --header "Authorization: Bearer ${TOKEN}" \
  --data '{"query":"query findById($internalId: ID) { findById(internalId: $internalId) { id, internalId, category, name, salesUnits, stock, profitMargin, daysInStock }}", "variables":{"internalId":"1"}}' | jq .

curl -sLf 'http://gateway:8090/product-dev/api/graphql' \
  --header 'Accept: application/json' \
  --header 'Content-Type: application/json' \
  --header "Authorization: Bearer ${TOKEN}" \
  --data '{
    "query": "query sortProducts($salesUnits: Float, $stock: Float, $profitMargin: Float, $daysInStock: Float, $page: Int, $size: Int, $withDetails: Boolean!) { sortProducts(salesUnits: $salesUnits, stock: $stock, profitMargin: $profitMargin, daysInStock: $daysInStock, page: $page, size: $size) { id @include(if: $withDetails) internalId @include(if: $withDetails) category @include(if: $withDetails) name salesUnits stock profitMargin daysInStock }}",
    "variables":{"salesUnits":0.0008,"stock":0.999,"profitMargin":0.0001,"daysInStock":0.0001,"page":0,"size":2,"withDetails":false}
    }' | jq .
```

</details>

## Operations (build, deploy)

For instructions on building and deploying this project, refer to
the [Operate - Readme](.operate/Readme.md) file.
