# camila-product-orchestrator-dev

> [Summary](#-summary)
  • [Usage](#-usage)
  • [Links](#-links)

## 📜 Summary

---

This environment is based on `docker-compose` and is designed for development purposes.

❗  **Important Note:** In this environment, the `chaos-monkey` profile is activated by default. This profile allows testing of [Chaos Engineering Principles](https://principlesofchaos.org/) on the `backend-product` service.

## 🌐 Usage

---

### Using Scripts

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

### Manually

The Docker images for the services need to be built beforehand:

  * docker.io/library/camila-config:1.0.0
  * docker.io/library/camila-discovery:1.0.0
  * docker.io/library/camila-gateway:1.0.0
  * docker.io/library/camila-product-api:1.0.0
  * docker.io/library/camila-admin:1.0.0

```bash
cd dev/compose

# Start the services in detached mode
docker-compose up -d --build --force-recreate

# List running services
docker-compose ps

# View logs (follow option shows live updates)
docker-compose logs mongodb backend-product gateway --follow

# Stop the services
docker-compose down
```

## 🔗 Links

---

* **Databases:**
  * [Couchbase database UI](http://localhost:8091/ui/index.html)
* **API:**
  * [Product API documentation](http://localhost:8080/product-dev/api/swagger-ui.html)
* **Gateway:**
  * [Gateway API documentation](http://localhost:8090/swagger-ui.html)
* **Discovery:**
  * [Service discovery UI](http://localhost:8761)
* **Admin:**
  * [Service admin UI](http://localhost:8100)
* **Config:**
  * [Config for admin - dev environment](http://localhost:8888/camila-admin/dev/main)
  * [Config for admin](http://localhost:8888/camila-admin/main)
  * [Config for gateway - dev environment](http://localhost:8888/camila-gateway/dev/main)
  * [Config for product API - dev environment](http://localhost:8888/camila-product-api/dev/main)
* **Keycloak (Authentication):**
  * [Keycloak admin console](http://keycloak:9191/admin/master/console) (Login: admin/admin1234)
  * [Keycloak OpenID configuration](http://keycloak:9191/realms/camila-realm/.well-known/openid-configuration)
* **Prometheus (Monitoring):**
  * [Prometheus monitoring dashboard](http://localhost:9090)
* **Grafana (Monitoring Visualization):**
  * [Grafana dashboard](http://localhost:3000) (Login: admin/admin)
* **Zipkin (Distributed Tracing):**
  * [Zipkin tracing UI](http://localhost:9411/zipkin)
* **Elasticsearch (Search Engine):**
  * [Elasticsearch access](http://localhost:9200/) (Login: elastic/changeme)
* **Kibana (Search Engine Visualization):**
  * [Kibana dashboard](http://localhost:5601/app/kibana_overview)
