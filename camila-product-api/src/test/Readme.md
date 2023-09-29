# camila-product-api-test

Se plantean 2 tipos de pruebas

* Pruebas unitarias con `mocks` [UT]
* Pruebas de integración utilizando base de datos embebida [IT]

## Arquitectura

```bash
📦api
 ┗ 📂product
   ┣ 📂application
   ┣ 📂infrastructure
   ┃ ┗ 📂persistence
   ┗ 📂presentation
```

## Pruebas locales con Junit

```bash
mvn clean test 
```

## Pruebas configurando aot

```bash
export SPRING_PROFILES_ACTIVE=loc
mvn clean spring-boot:process-test-aot
```

## Generador de datos

Se ha creado un generador de datos aleatorios que permite poblar la BBDD para pruebas de rendimiento

[RandomDataGenerator](java/com/camila/api/product/infrastructure/persistence/RandomDataGenerator.java)
