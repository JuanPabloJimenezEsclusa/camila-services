# camila-product-api-test

Se plantean 5 tipos de pruebas

| Tipo | Detalles |
|------|----------|
| Pruebas unitarias | Se utiliza `mocks` y el plugin: `surefire` [UT] |
| Pruebas de integración | Se utiliza una base de datos embebida `de.flapdoodle.embed.mongo` y el plugin: `failsafe` [IT] |
| Pruebas de arquitectura | Con la librería: `ArchUnit` [AT] |
| Pruebas de benchmark | Se utiliza: [Java Microbenchmark Harness](https://github.com/openjdk/jmh) [JMH-T] |
| Pruebas de rendimiento | Se utiliza: [Jmeter](https://jmeter.apache.org) |

## Arquitectura

```txt
📦api
 ┣ 📂architecture (Architecture tests)
 ┣ 📂benchmark (Benchmark tests)
 ┣ 📂product
 ┃ ┣ 📂application
 ┃ ┃ ┗ 📂port
 ┃ ┃   ┗ 📂input (Unit tests)
 ┃ ┗ 📂framework
 ┃   ┗ 📂adapter
 ┃     ┣ 📂input
 ┃     ┃ ┗ 📂rest (Integration tests)
 ┃     ┗ 📂output
 ┃       ┗ 📂repository (Integration tests)
 ┗ 📜ProductApiApplicationTests.java
```

## Operar

### Pruebas unitarias y de arquitectura

```bash
mvn clean test 
```

### Pruebas unitarias con aot

```bash
export SPRING_PROFILES_ACTIVE=loc
mvn clean spring-boot:process-test-aot
```

### Pruebas de integración y benchmark

```bash
mvn clean verify
```

### Análisis de código

* Chequeo de dependencias: [dependency-check-maven](https://jeremylong.github.io/DependencyCheck/dependency-check-maven/)
* Análisis de errores: [error-prone](https://github.com/google/error-prone)

```bash
mvn clean verify site -P check-dependency,error-prone
```

> [file:///${WORKSPACE}/camila-services/camila-product-api/target/site/project-info.html]()
 
### Pruebas de rendimiento

* Configuración: [Readme](./resources/scripts/jmeter)

```bash
# opcional, para modificar valores por defecto
export JMETER_TEST_PATH="/tmp/results"
export THREADS=100
export RAMP_UP=20 
export LOOPS=5

# ejecutar plan de pruebas
./resources/scripts/jmeter/run.sh
```

## Generador de datos

Existe un generador de datos aleatorios que permite poblar la BBDD para pruebas de rendimiento

[RandomDataGenerator](java/com/camila/api/product/framework/adapter/output/repository/RandomDataGenerator.java)
