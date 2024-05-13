# camila-product-api

API Rest de productos

## Pre-condiciones

* JDK >= 21
* Docker >= 24.0.6
* Maven >= 3.8.8
* Spring >= 6.x
* Spring-boot >= 3.2.x
* MongoDB >= 7.x
* Native Image compilation
  * GraalVM >= 21.0.1+12.1
  * GCC >= (linux, x86_64, 11.4.0)
    * `zlib1g-dev`
  
## Arquitectura

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
 ┃     ┃ ┗ 📂rest
 ┃     ┗ 📂output
 ┃       ┗ 📂repository
 ┗ 📜ProductApiApplication.java
```

![Arquitectura-hexagonal](.docs/architecture/camila-product-api-architecture-v1.svg "Diagrama Hexagonal")

## Enlaces

* API DOC: <http://localhost:8080/product-dev/api/swagger-ui.html>

## Ejemplos de petición API

```bash
curl -X 'GET' \
  'http://localhost:8080/product-dev/api/products?salesUnits=0.80&stock=0.20&page=0&size=20' \
  -H 'accept: application/json'
  
curl -X 'GET' \
  'http://localhost:8080/product-dev/api/products/1' \
  -H 'accept: application/json'
```

## Pruebas

[Test - Readme](./src/test/Readme.md)

## Operaciones (build, deploy)

[Operar - Readme](.operate/Readme.md)

## Ejemplo de consulta de `products` con filtro de `sales units` y `stock` ponderada

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
            ]
          }
        ]
      }
    }
  },
  {
    $sort: {
      weightedScore: -1
    }
  }
]);
```
