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
