#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

echo -e "${SEPARATOR}🗑️ Remove image. ${SEPARATOR}"
docker rmi docker.io/library/camila-product-api:1.0.0 || true

echo -e "${SEPARATOR}🔨 Compile and build the image. ${SEPARATOR}"
export SPRING_PROFILES_ACTIVE=int
mvn clean spring-boot:build-image \
  -Dmaven.build.cache.enabled=false \
  -Dmaven.test.skip=true \
  -f ../../../../../camila-product-api/pom.xml

echo -e "${SEPARATOR}🐳 A simple way to load the image into the kind cluster. ${SEPARATOR}"
kind load docker-image docker.io/library/camila-product-api:1.0.0 --name kind-cluster

echo -e "${SEPARATOR}📦 Apply the k8s resources. ${SEPARATOR}"
kubectl apply -f ./camila-product-api.yml

echo -e "${SEPARATOR}⏳ Wait for the pod to be ready. ${SEPARATOR}"
kubectl wait --for=condition=ready pod -l app=camila-product-api -n camila-product-api-ns --timeout=5m

echo -e "${SEPARATOR}📋 Get the resources. ${SEPARATOR}"
kubectl get all,pv,pvc,resourcequotas,ingress -n camila-product-api-ns -o wide --show-labels
