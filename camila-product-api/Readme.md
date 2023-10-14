# camila-product-api

API Rest de productos

## Pre-condiciones

* JDK >= 17
* Docker >= 24.0.6
* maven >= 3.9.4
* GraalVM >= 22.3.2
* Spring >= 6.x
* Spring-boot >= 3.1.x

## Arquitectura

```txt
📦api
 ┣ 📂product
 ┃ ┣ 📂application
 ┃ ┣ 📂domain
 ┃ ┃ ┣ 📂model
 ┃ ┃ ┗ 📂service
 ┃ ┣ 📂infrastructure
 ┃ ┃ ┗ 📂persistence
 ┃ ┗ 📂presentation
 ┗ 📜ProductApiApplication.java
```

![Arquitectura-hexagonal](.docs/architecture/camila-product-api-architecture-v1.png "Diagrama Hexagonal")

## Enlaces

* API DOC (dev): <http://localhost:8080/product-dev/api/swagger-ui.html>

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
