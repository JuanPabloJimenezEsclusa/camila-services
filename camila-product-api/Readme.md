# camila-product-api

> Microservice example

Implement:
- Paradigm: [Reactive](https://projectreactor.io/learn)
- Architecture: [Hexagonal](https://alistair.cockburn.us/hexagonal-architecture/)
- Communication: 
  - [Rest](https://en.wikipedia.org/wiki/REST)
  - [Graphql](https://graphql.org/)
  - [Websocket](https://en.wikipedia.org/wiki/WebSocket)
  - [RSocket](https://rsocket.io/)
  - [GRPC](https://grpc.io/docs/what-is-grpc/core-concepts/)

---

## Pre-conditions

* JDK >= 21
* Docker >= 24.0.6
* Maven >= 3.8.8
* Spring >= 6.x
* Spring-boot >= 3.3.x
* MongoDB >= 7.x
* Couchbase >= 7.x
* Native Image compilation
  * GraalVM >= 21.0.1+12.1
  * GCC >= (linux, x86_64, 11.4.0)
    * `zlib1g-dev`

---

## Architecture

```txt
📦api
 ┣ 📂product
 ┃ ┣ 📂domain
 ┃ ┣ 📂application
 ┃ ┃ ┣ 📂usercase
 ┃ ┃ ┗ 📂port
 ┃ ┃   ┣ 📂input
 ┃ ┃   ┗ 📂output
 ┃ ┗ 📂framework
 ┃   ┗ 📂adapter
 ┃     ┣ 📂input
 ┃     ┃ ┣ 📂security
 ┃     ┃ ┣ 📂rest
 ┃     ┃ ┣ 📂graphql
 ┃     ┃ ┣ 📂websocket
 ┃     ┃ ┣ 📂rsocket
 ┃     ┃ ┗ 📂grpc
 ┃     ┗ 📂output
 ┃       ┣ 📂mongo
 ┃       ┗ 📂couchbase
 ┗ 📜ProductApiApplication.java
```

![Hexagonal-architecture](.docs/architecture/camila-product-api-architecture-v1.svg "Hexagonal Diagram")

---

## Links

* Rest API DOC: <http://localhost:8080/product-dev/api/swagger-ui.html>
* Graphql API DOC: <http://localhost:8080/product-dev/api/graphiql>

---

## API request examples

### Rest API

![camila-product-api-rest-example.gif](.docs/examples/camila-product-api-rest-example.gif)

```bash
curl -X 'GET' \
  'http://localhost:8080/product-dev/api/products/1' \
  -H 'Accept: application/json'
  
curl -X 'GET' \
  'http://localhost:8080/product-dev/api/products?salesUnits=0.80&stock=0.20&page=0&size=20' \
  -H 'Accept: application/json'

# Server-sent Event (SSE)
curl -X 'GET' \
  'http://localhost:8080/product-dev/api/products?salesUnits=0.80&stock=0.20&page=0&size=20' \
  -H 'Accept: text/event-stream'

# NDJSON: https://github.com/ndjson/ndjson-spec
curl -X 'GET' \
  'http://localhost:8080/product-dev/api/products?salesUnits=0.80&stock=0.20&page=0&size=20' \
  -H 'Accept: application/x-ndjson'
```

> [3 techniques to stream JSON in Spring WebFlux](https://nurkiewicz.com/2021/08/error-handling-in-json-streaming-with-webflux.html)

### Graphql API

![camila-product-api-graphql-example.gif](.docs/examples/camila-product-api-graphql-example.gif)

```bash
curl --location 'http://localhost:8080/product-dev/api/graphql' \
  --header 'Accept: application/json' \
  --header 'Content-Type: application/json' \
  --data-raw '{"query":"query sortProducts($salesUnits: Float, $stock: Float, $page: Int, $size: Int, $withDetails: Boolean!) {\n    sortProducts(salesUnits: $salesUnits, stock: $stock, page: $page, size: $size) {\n        id @include(if: $withDetails)\n        internalId @include(if: $withDetails)\n        category @include(if: $withDetails)\n        name\n        salesUnits\n        stock\n    }\n}\n","variables":{"salesUnits":0.001,"stock":0.999,"page":0,"size":2,"withDetails":false}}'

curl --location 'http://localhost:8080/product-dev/api/graphql' \
  --header 'Accept: application/json' \
  --header 'Content-Type: application/json' \
  --data '{"query":"query findById($internalId: ID) {\n  findById(internalId: $internalId) {\n    id, internalId, category, name, salesUnits, stock\n  }\n}\n","variables":{"internalId":"1"}}'
```

### Websocket

![camila-product-api-websocket-example.gif](.docs/examples/camila-product-api-websocket-example.gif)

```bash
# https://github.com/vi/websocat
echo '{ "method": "FIND_BY_INTERNAL_ID", "internalId": "1" }' \
  | websocat -n1 ws://localhost:8080/product-dev/api/ws/products

echo '{ "method": "SORT_PRODUCTS", "salesUnits": "0.001", "stock": "0.999", "page": "0", "size": "100" }' \
  | websocat --no-close ws://localhost:8080/product-dev/api/ws/products

docker run --rm -it \
  --network=host \
  ghcr.io/vi/websocat:nightly \
  ws://localhost:8080/product-dev/api/ws/products 
{ "method": "SORT_PRODUCTS", "salesUnits": "0.999", "stock": "0.001", "page": "0", "size": "100" }
```

### RSocket

```bash
export RSOCKET_SERVER_URL="ws://localhost:7000/product-dev/api/rsocket"
./.docs/api-rsocket-request.sh
```

---

## Testing

[Tests - Readme](src/test/Readme.md)

---

## Operations (build, deploy)

[Operations - Readme](.operate/Readme.md)

---

## Notes:

### Example of aggregating `products` with `sales units` and weighted `stock` filter in `mongodb`

```bash
db.products.aggregate([
  {
    $addFields: {
      weightedScore: {
        $add: [
          {
            $multiply: ["$salesUnits", 0.80]
          },
          {
            $multiply: [
              {
                $divide: [
                  {
                    $sum: {
                      $map: {
                        input: { $objectToArray: "$stock" },
                        as: "size",
                        in: "$$size.v"
                      }
                    }
                  },
                  {
                    $size: { $objectToArray: "$stock" }
                  }
                ]
              },
              0.20
            ]}]}}},
  {
    $sort: {
      weightedScore: -1
    }}
]);
```
