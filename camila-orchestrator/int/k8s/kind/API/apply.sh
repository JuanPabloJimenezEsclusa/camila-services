#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

cd "$(dirname "$0")"

# opcional
#docker rmi docker.io/library/camila-product-api:1.0.0

# compilar imagen
export SPRING_PROFILES_ACTIVE=int
mvn clean spring-boot:build-image \
  -Dmaven.build.cache.enabled=false \
  -Dmaven.test.skip=true \
  -f ./../../../../camila-product-api/pom.xml

# un camino simple para carga una imagen a los registros locales de los nodos del cluster
kind load docker-image docker.io/library/camila-product-api:1.0.0 --name kind-cluster

# aplicar los primitivos
kubectl apply -f ./camila-product-api.yml

# consultar el namespace
kubectl get all,pv,pvc,resourcequotas,ingress -n camila-product-api-ns -o wide --show-labels
