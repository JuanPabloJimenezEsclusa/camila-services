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
