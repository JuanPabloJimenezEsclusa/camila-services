
<p style="text-align: center">
  <img src=".docs/logo.svg" alt="camila-logo">
</p>

<p style="text-align: center">
  <bold>Java</bold> Side Project
</p>

<p style="text-align: center">
  <a href="https://alistair.cockburn.us/hexagonal-architecture/"><img src="https://img.shields.io/badge/Architecture-Hexagonal-brightgreen.svg?style=for-the-badge" alt="Hexagonal Architecture" /></a>
  <a href="https://www.reactivemanifesto.org/"><img src="https://img.shields.io/badge/Programming%20Paradigm-Reactive-blue.svg?style=for-the-badge" alt="Reactive Paradigm" /></a>
  <a href="https://microservices.io/"><img src="https://img.shields.io/badge/Architectural%20Style-Microservices-purple.svg?style=for-the-badge" alt="Microservices Style" /></a>
  <a href="https://oauth.net/2/"><img src="https://img.shields.io/badge/Security-OAuth2-yellow.svg?style=for-the-badge" alt="OAuth2 Standard" /></a>
  <a href="https://martinfowler.com/articles/serverless.html"><img src="https://img.shields.io/badge/Deploy%20Approach-Serveless-black.svg?style=for-the-badge" alt="Serveless Standard" /></a>
</p>

## Technologies

| Development                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       | Testing                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        | Deployment                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [![OpenJDK](https://img.shields.io/badge/OpenJDK-%3E%3D21-005571.svg)](https://adoptium.net/es/temurin/releases/) <br> [![GraalVM](https://img.shields.io/badge/GraalVM-%3E%3D21.0.1-005571.svg)](https://www.graalvm.org/downloads/) <br> [![Maven](https://img.shields.io/badge/Maven-%3E%3D3.8.8-005571.svg)](https://maven.apache.org/) <br> [![Spring](https://img.shields.io/badge/Spring-%3E%3D6.x-brightgreen.svg)](https://spring.io/) <br> [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-%3E%3D3.2.x-brightgreen.svg)](https://spring.io/boot) <br> [![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-%3E%3D2023.0.x-brightgreen.svg)](https://spring.io/cloud) <br> [![MongoDB](https://img.shields.io/badge/MongoDB-%3E%3D7.x-cyan.svg)](https://www.mongodb.com/) <br> [![Couchbase](https://img.shields.io/badge/Couchbase-%3E%3D7.x-blue.svg)](https://www.couchbase.com/) | [![JUnit5](https://img.shields.io/badge/JUnit5-%3E%3D5.10.2-orange.svg)](https://junit.org/junit5/) <br> [![Cucumber](https://img.shields.io/badge/Cucumber-%3E%3D7.17.0-orange.svg)](https://cucumber.io/) <br> [![Pitest](https://img.shields.io/badge/Pitest-%3E%3D1.21.1-orange.svg)](https://pitest.org/) <br> [![ChaosMonkey](https://img.shields.io/badge/ChaosMonkey-%3E%3D3.1.0-orange.svg)](https://codecentric.github.io/chaos-monkey-spring-boot/) <br> [![ArchUnit](https://img.shields.io/badge/ArchUnit-%3E%3D1.2.1-orange.svg)](https://www.archunit.org/) <br> [![JMeter](https://img.shields.io/badge/JMeter-%3E%3D5.6.2-orange.svg)](https://jmeter.apache.org/) <br> [![TestContainers](https://img.shields.io/badge/Testcontainers-%3E%3D1.19.8-orange.svg)](https://testcontainers.com/) | [![Docker](https://img.shields.io/badge/Docker-%3E%3D26.1.3-brown.svg)](https://www.docker.com/) <br> [![Docker-compose](https://img.shields.io/badge/Docker%20Compose-%3E%3D2.27.0-brown.svg)](https://docs.docker.com/compose/install/) <br> [![Kubernetes](https://img.shields.io/badge/Kubernetes-%3E%3D1.30.1-brown.svg)](https://kubernetes.io/releases/) <br> [![Knative](https://img.shields.io/badge/Knative-%3E%3D1.10.2-brown.svg)](https://github.com/knative/serving/releases/) |

## Components

| Component                                   | Description                                                      |
|---------------------------------------------|------------------------------------------------------------------|
| [camila-product-api](/camila-product-api)   | Contains a microservice that exposes the product query           |
| [camila-discovery](/camila-discovery)       | Contains a service discoverer                                    |
| [camila-gateway](/camila-gateway)           | Contains a gateway for services                                  |
| [camila-config](/camila-config)             | Contains a central service configurator                          |
| [camila-admin](/camila-admin)               | Contains a service manager (UI)                                  |
| [camila-orchestrator](/camila-orchestrator) | Contains configuration as code to orchestrate project deployment |

## Architecture diagrams

![Architecture-C1](.docs/architecture/camila-service-da-v1-C1.svg "Diagram C1")

![Architecture-C2](.docs/architecture/camila-service-da-v1-C2.svg "Diagram C2")

## Domain Storytelling

At **camila.shopping**, the need to improve the organization and presentation of products has been recognized. To address this challenge, the development of a classification algorithm that optimizes the user experience when searching for such products has been proposed.
![domain-storytelling](.docs/architecture/camila-shopping-domain-v1.dst.svg "Diagram WDS")

---

**Problem:**
The diversity of products on **camila.shopping** is impressive, but the way they are presented can be improved. The task is to organize these products effectively within their respective categories to make it easier for customers to search.

**Solution:**
The proposed solution is a classification algorithm that uses a weighted combination of metrics, in this case: the number of units sold and the stock ratio. The weighting will assign, for example, 80% weight to the number of units sold. The remaining 20% will be assigned to the stock ratio, these weights being variable in each query.

**Presentation of Results:**
The organized information will be exposed through a REST API. This interface will provide an efficient means to access the list of products sorted according to the defined metrics.

**Implementation:**
The algorithm will be integrated into the **camila.shopping** infrastructure and will communicate with existing systems to collect sales and stock data. Classified information will be available for consumption through an API, providing users with easy and direct access to organized products.

**Expected Impact:**
With this improvement, it is expected to optimize the user experience, increase efficiency in product search and increase customer satisfaction at **camila.shopping**. Implementing the ranking algorithm is a strategic step to stay ahead in the competitive world of e-commerce.

---

Metrics:

# camila-product-orchestrator-dev

Basado en `docker-compose`

❗ En este ambiente se activa por defecto el perfil `chaos-monkey` que permite probar [Principios de la Ingeniería del Caos](https://principlesofchaos.org/) en el servicio `backend-product`

## Operaciones

### a través de scripts

Para el correcto funcionamiento local, es necesario modificar el archivo: `/etc/hosts` para enlazar la IP: `127.0.0.1` con el `hostname` de los servicios del `docker-compose`

```bash
cd dev/compose

# actualizar el archivo /etc/hosts
sudo ./mappingHosts.sh

# iniciar los servicios
./start.sh

# detener los servicios
./stop.sh
```

### de forma manual

Es necesario que estén construidas las imágenes de los servicios:

* docker.io/library/camila-config:1.0.0
* docker.io/library/camila-discovery:1.0.0
* docker.io/library/camila-gateway:1.0.0
* docker.io/library/camila-product-api:1.0.0
* docker.io/library/camila-admin:1.0.0

```bash
cd dev/compose

# iniciar los servicios
docker-compose up -d

# mostrar los servicios
docker-compose ps

# ver logs
docker-compose logs mongodb backend-product gateway --follow

# detener los servicios
docker-compose down
```

## Enlaces

* API
    * <http://localhost:8080/product-dev/api/swagger-ui.html>
* Gateway
    * <http://localhost:8090/swagger-ui.html>
* Discovery
    * <http://localhost:8761>
* Admin
    * <http://localhost:8100>
* Config
    * <http://localhost:8888/camila-admin/dev/main>
    * <http://localhost:8888/camila-admin/main>
    * <http://localhost:8888/camila-gateway/dev/main>
    * <http://localhost:8888/camila-product-api/dev/main>
* Keycloak
    * <http://keycloak:9191/admin/master/console> (admin/admin1234)
* Prometheus
    * <http://localhost:9090>
* Grafana
    * <http://localhost:3000> (admin/admin)
* Zipkin
    * <http://localhost:9411/zipkin>
* Elasticsearch
    * <http://localhost:9200/> (elastic/changeme)
* Kibana
    * <http://localhost:5601/app/kibana_overview>


---

Product data sample:

| id | name                          | sales_units | stock                |
|----|-------------------------------|-------------|----------------------|
| 1  | V-NECH BASIC SHIRT            | 100         | S: 4 / M:9 / L:0     |
| 2  | CONTRASTING FABRIC T-SHIRT    | 50          | S: 35 / M:9 / L:9    |
| 3  | RAISED PRINT T-SHIRT          | 80          | S: 20 / M:2 / L:20   |
| 4  | PLEATED T-SHIRT               | 3           | S: 25 / M:30 / L:10  |
| 5  | CONTRASTING LACE T-SHIRT      | 650         | S: 0 / M:1 / L:0     |
| 6  | SLOGAN T-SHIRT                | 20          | S: 9 / M:2 / L:5     |

## Package

```bash
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-"loc"}"

# jars
mvn deploy \
  -Dmaven.test.skip=true  -f ./pom.xml
# images
mvn spring-boot:build-image \
  -Dmaven.test.skip=true  -f ./pom.xml
```
