#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")"

# compilar y crear imagen
export SPRING_PROFILES_ACTIVE=dev
mvn clean spring-boot:build-image \
  -Dmaven.test.skip=true \
  -f ./../../../camila-product-api/pom.xml

# iniciar servicios
docker-compose up -d

# mostrar servicios
docker-compose ps
