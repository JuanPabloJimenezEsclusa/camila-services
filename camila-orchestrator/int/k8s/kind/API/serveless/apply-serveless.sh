#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

echo -e "${SEPARATOR} 🗑️ Remove image. ${SEPARATOR}"
docker rmi \
  docker.io/library/camila-product-api-serveless:1.0.0 \
  kind-registry:5000/camila-product-api-serveless:1.0.0 \
  172.28.0.6:5000/camila-product-api-serveless:1.0.0 --force || true

echo -e "${SEPARATOR} 🔨 Compile and build the image. ${SEPARATOR}"
SPRING_PROFILES_ACTIVE=int mvn --no-transfer-progress --batch-mode \
  clean install \
  -Dgpg.skip=true \
  -Dmaven.build.cache.enabled=false \
  -Dmaven.test.skip=true \
  -f ../../../../../../pom.xml
SPRING_PROFILES_ACTIVE=int mvn compile jib:dockerBuild \
  -P jib \
  -Dmaven.test.skip=true \
  -f ../../../../../../camila-product-api/camila-product-api-infrastructure/driving/camila-product-api-infrastructure-boot/pom.xml

echo -e "${SEPARATOR} 🔨 Tag and push the image in local registry. ${SEPARATOR}"
docker tag docker.io/library/camila-product-api-serveless:1.0.0 kind-registry:5000/camila-product-api-serveless:1.0.0
docker push kind-registry:5000/camila-product-api-serveless:1.0.0

echo -e "${SEPARATOR} 📦 Apply the k8s resources. ${SEPARATOR}"
kubectl apply -f ./camila-product-api-serveless.yml

echo -e "${SEPARATOR} ⏳ Wait for the ksvc to be ready. ${SEPARATOR}"
kubectl wait --for=condition=ready --timeout=5m ksvc/camila-product-api-serveless -n camila-product-api-serveless-ns

echo -e "${SEPARATOR} 📋 Get the ksvc. ${SEPARATOR}"
kubectl get ksvc -n camila-product-api-serveless-ns
