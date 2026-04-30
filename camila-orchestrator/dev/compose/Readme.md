# camila-product-orchestrator-dev

> [Summary](#-summary)
  • [Usage](#-usage)
  • [Links](#-links)
  • [How to Validate the Changes](#-how-to-validate-the-changes)

## 📜 Summary

---

This environment is based on `docker-compose` and is designed for development purposes.

❗  **Important Note:** In this environment, the `chaos-monkey` profile is activated by default. This profile allows testing of [Chaos Engineering Principles](https://principlesofchaos.org/) on the `backend-product` service.

## 🌐 Usage

---

<details>
<summary><strong>Expand Usage</strong></summary>

### Using Scripts

For the local environment to function correctly, you need to modify the `/etc/hosts` file to map the IP address `127.0.0.1` to the hostnames of the services defined in the `docker-compose` file.

```bash
cd dev/compose

# Update the /etc/hosts file (requires sudo permissions)
sudo ./mappingHosts.sh

# Start the services
./start.sh buildProjects=true

# Stop the services
./stop.sh removeImages=true
```

### Manually

The Docker images for the services need to be built beforehand:

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
docker-compose logs mongodb couchbase redis --follow 
docker-compose logs admin consul gateway backend-product --follow
docker-compose logs fluentd elasticsearch kibana --follow

# Stop the services
docker-compose down
```

</details>

## 🔗 Links

---

<details>
<summary><strong>Expand Links</strong></summary>

* **Databases:**
  * [Couchbase database UI](http://localhost:8091/ui/index.html) (Administrator/password)
  * [Redis Insights](http://localhost:5540/) (default/camila)
* **API:**
  * [Product API documentation 1](http://localhost:8080/product-dev/api/swagger-ui.html)
  * [Product API documentation 2](http://localhost:8081/product-dev/api/swagger-ui.html)
  * [Product API documentation 3](http://localhost:8082/product-dev/api/swagger-ui.html)
* **Gateway:**
  * [Gateway API documentation](http://localhost:8090/swagger-ui.html)
* **Consul (Discovery + Config):**
  * [Consul UI](http://localhost:8500/ui/)
  * [Consul KV config](http://localhost:8500/ui/dc1/kv/config/)
* **Admin:**
  * [Service admin UI](http://localhost:8100)
* **Keycloak (Authentication):**
  * [Keycloak admin console](http://keycloak:9191/admin/master/console) (Login: admin/admin1234)
  * [Keycloak OpenID configuration](http://keycloak:9191/realms/camila-realm/.well-known/openid-configuration)
* **Cadvisor (Monitoring):**
  * [Cadvisor dashboard](http://localhost:8880/containers)
* **Prometheus (Monitoring):**
  * [Prometheus monitoring dashboard](http://localhost:9090)
  * [Node Exporter metrics](http://localhost:9100/metrics)
  * [Alertmanager UI](http://localhost:9093)
* **Grafana (Monitoring Visualization):**
  * [Grafana dashboard](http://localhost:3000) (Login: admin/admin)
* **Zipkin (Distributed Tracing):**
  * [Zipkin tracing UI](http://localhost:9411/zipkin)
* **Elasticsearch (Search Engine):**
  * [Elasticsearch access](http://localhost:9200/) (Login: elastic/changeme)
* **Kibana (Search Engine Visualization):**
  * [Kibana dashboard](http://localhost:5601/app/kibana_overview)
* **Utilities:**
  * [Mailpit](http://localhost:8025/)

</details>

## 🧪 How to Validate the Changes

---

<details>
<summary><strong>Expand Validate</strong></summary>

```bash
# During the test, use this to check circuit breaker status (should remain CLOSED)
curl -Ls http://localhost:8090/actuator/health \
  | jq '.components.circuitBreakers.details.fallbackCircuitBreaker.details'
```

</details>
