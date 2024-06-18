# camila-product-orchestrator-dev

This environment is based on `docker-compose` and is designed for development purposes.

❗  **Important Note:** In this environment, the `chaos-monkey` profile is activated by default. This profile allows testing of [Chaos Engineering Principles](https://principlesofchaos.org/) on the `backend-product` service.

## Operations

**Using Scripts**

For the local environment to function correctly, you need to modify the `/etc/hosts` file to map the IP address `127.0.0.1` to the hostnames of the services defined in the `docker-compose` file.

```bash
cd dev/compose

# Update the /etc/hosts file (requires sudo permissions)
sudo ./mappingHosts.sh

# Start the services
./start.sh

# Stop the services
./stop.sh
```

**Manually**

The Docker images for the services need to be built beforehand:

* docker.io/library/camila-config:1.0.0
* docker.io/library/camila-discovery:1.0.0
* docker.io/library/camila-gateway:1.0.0
* docker.io/library/camila-product-api:1.0.0
* docker.io/library/camila-admin:1.0.0

```bash
cd dev/compose

# Start the services in detached mode
docker-compose up -d

# List running services
docker-compose ps

# View logs (follow option shows live updates)
docker-compose logs mongodb backend-product gateway --follow

# Stop the services
docker-compose down
```

## Links

* **Databases:**
  * <http://localhost:8091/ui/index.html> (Link to the database UI)
* **API:**
  * <http://localhost:8080/product-dev/api/swagger-ui.html> (Link to the product API documentation)
* **Gateway:**
  * <http://localhost:8090/swagger-ui.html> (Link to the gateway API documentation)
* **Discovery:**
  * <http://localhost:8761> (Link to the service discovery UI)
* **Admin:**
  * <http://localhost:8100> (Link to the admin UI)
* **Config:**
  * <http://localhost:8888/camila-admin/dev/main> (Link to config service - dev environment)
  * <http://localhost:8888/camila-admin/main> (Link to config service)
  * <http://localhost:8888/camila-gateway/dev/main> (Link to config for gateway - dev environment)
  * <http://localhost:8888/camila-product-api/dev/main> (Link to config for product API - dev environment)
* **Keycloak (Authentication):**
  * <http://keycloak:9191/admin/master/console> (Username: admin/admin1234) - Keycloak admin console
  * <http://keycloak:9191/realms/camila-realm/.well-known/openid-configuration> (Link to Keycloak OpenID configuration)
* **Prometheus (Monitoring):**
  * <http://localhost:9090> (Link to the Prometheus monitoring dashboard)
* **Grafana (Monitoring Visualization):**
  * <http://localhost:3000> (Username: admin/admin) - Grafana dashboard (requires login)
* **Zipkin (Distributed Tracing):**
  * <http://localhost:9411/zipkin> (Link to the Zipkin distributed tracing UI)
* **Elasticsearch (Search Engine):**
  * <http://localhost:9200/> (Username: elastic/changeme) - Elasticsearch access (requires login)
* **Kibana (Search Engine Visualization):**
  * <http://localhost:5601/app/kibana_overview> (Link to the Kibana search engine visualization dashboard)
