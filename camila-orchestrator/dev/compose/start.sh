#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")"

workspace="$(pwd)"
baseProjectPath="../../../"

# variables de entorno
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-"dev"}"
export GRAALVM_HOME="${GRAALVM_HOME:-"/usr/lib/jvm/graalvm-jdk-21.0.1+12.1"}"

__buildProjects() {
  # vamos a la base del proyecto (camila-services)
  cd "${workspace}/${baseProjectPath}"

  # compilar y empaquetar en imágenes todos los módulos del proyecto
  #mvn clean spring-boot:build-image -Dmaven.test.skip=true -f ./pom.xml

  # compilar y empaquetar en imágenes cada módulos, comentar para saltar
  #mvn clean spring-boot:build-image -Dmaven.test.skip=true -f ./camila-admin/pom.xml
  #mvn clean spring-boot:build-image -Dmaven.test.skip=true -f ./camila-config/pom.xml
  #mvn clean spring-boot:build-image -Dmaven.test.skip=true -f ./camila-discovery/pom.xml
  #mvn clean spring-boot:build-image -Dmaven.test.skip=true -f ./camila-gateway/pom.xml
  #mvn clean spring-boot:build-image -Dmaven.test.skip=true -f ./camila-product-api/pom.xml #-Pnative
}

__initServices() {
  cd "${workspace}"

  # iniciar los servicios
  docker-compose up -d
  # mostrar los servicios
  docker-compose ps	
}

main() {
  __buildProjects
  __initServices
}

time main | tee result-dev-start.log
