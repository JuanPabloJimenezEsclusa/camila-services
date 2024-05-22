# camila-orchestrator

Contiene la configuración como código que permite desplegar el servicio en un ambiente de contenedores y cloud

## Pre-requisitos

* docker-compose >= v2.27.0 (o embebido en docker)
* kubernetes >= 1.30.0
* linux

## Entornos

| Entorno     | Descripción                |
|-------------|----------------------------|
| [dev](./dev/) | Orquestación básica de contenedores con `docker-compose` que permite crear un entorno de despliegue con todos los componentes del servicio |
| [int](./int/) | Orquestación en `k8s` utilizando ó deployments ó serveless `knative` |

## Infraestructura

| Proveedor                           | Descripción                |
|-------------------------------------|----------------------------|
| [clusters - kind](./clusters/kind/) | Configuración de un cluster `k8s` utilizando el proyecto: [kubernetes in docker](https://kind.sigs.k8s.io/) |

## Arquitectura

```txt
📦camila-orchestrator
 ┣ 📂clusters
 ┃ ┗ 📂kind (install.sh - start-cluster.sh - stop-cluster.sh)
 ┣ 📂dev
 ┃ ┗ 📂compose (start.sh - stop.sh)
 ┣ 📂int
 ┃ ┗ 📂k8s
 ┃   ┣ 📂API
 ┃   ┃ ┗ 📂serveless (apply-serveless.sh - delete-serveless.sh)
 ┃   ┗ 📂DDBB (apply.sh - delete.sh)
 ┗ 📜Readme.md
```
