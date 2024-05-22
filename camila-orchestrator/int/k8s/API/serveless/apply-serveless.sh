#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")"

# opcional
docker rmi docker.io/library/camila-product-api-serveless:1.0.0 || true

# compilar imagen
export SPRING_PROFILES_ACTIVE=int
mvn compile jib:dockerBuild \
  -P jib \
  -Dmaven.test.skip=true \
  -f ./../../../../../camila-product-api/pom.xml

# un camino simple para carga una imagen a los registros locales de los nodos del cluster
kind load docker-image docker.io/library/camila-product-api-serveless:1.0.0 --name kind-cluster

# aplicar los primitivos
kubectl apply -f ./camila-product-api-serveless.yml

# consultar el namespace
kubectl get ksvc -n camila-product-api-serveless-ns
