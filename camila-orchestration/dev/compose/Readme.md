# camila-product-orchestration-dev

La orquestación en este entorno se basa en `docker-compose`


## Operaciones

```bash
cd dev

# iniciar servicios
docker-compose up -d

# mostrar servicios
docker-compose ps

# ver logs
docker-compose logs mongodb backend-product gateway --follow

# detener servicios
docker-compose down
```
