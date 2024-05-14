# camila-product-api-test

Se plantean 7 tipos de pruebas

| Tipo                            | Detalles                                                                                       |
|---------------------------------|------------------------------------------------------------------------------------------------|
| Pruebas unitarias               | Se utilizan `mocks` y el plugin: `surefire` [UT]                                               |
| Pruebas de integración          | Se utiliza una base de datos embebida `de.flapdoodle.embed.mongo` y el plugin: `failsafe` [IT] |
| Pruebas de arquitectura         | Se utiliza la librería: `ArchUnit` [AT]                                                        |
| Pruebas de mutación             | Se utiliza el plugin: [Pitest](https://github.com/pitest/pitest-junit5-plugin.git)             |
| Pruebas de comportamiento       | Se utiliza: [Cucumber](https://cucumber.io/docs/guides/)                                       |
| Pruebas de rendimiento (jmh)    | Se utiliza: [Java Microbenchmark Harness](https://github.com/openjdk/jmh) [JMH-T]              |
| Pruebas de rendimiento (jmeter) | Se utiliza: [Jmeter](https://jmeter.apache.org)                                                |

## Arquitectura

```txt
📦api
 ┣ 📂architecture (Architecture tests)
 ┣ 📂benchmark (Benchmark tests)
 ┣ 📂behaviour (Behaviuor tests)
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

### Pruebas de integración y benchmarking

```bash
mvn clean verify
```

### Pruebas de mutación

```bash
mvn clean test -P pitest
```

> Reporte: [./target/pit-reports/index.html](./../../target/pit-reports/index.html)

### Análisis de código

* Chequeo de dependencias: [dependency-check-maven](https://jeremylong.github.io/DependencyCheck/dependency-check-maven/)
* Análisis de errores: [error-prone](https://github.com/google/error-prone)

```bash
mvn clean verify site -P check-dependency,error-prone
```

> Reporte: [./target/site/project-info.html](./../../site/project-info.html)
 
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
