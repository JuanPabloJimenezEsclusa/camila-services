#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")/.."

# compilar nativo
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-"loc"}"
export GRAALVM_HOME="${GRAALVM_HOME:-"/usr/lib/jvm/graalvm-jdk-22.0.1+8.1"}"

# solo para compilar/empaquetar el artefacto nativo
mvn clean package \
  -Pnative \
  -Dmaven.test.skip=true \
  -f ./pom.xml | tee result-package-native.log

# compilar/empaquetar y construir imagen para contenedores
mvn spring-boot:build-image \
  -Pnative \
  -Dmaven.test.skip=true \
  -f ./pom.xml | tee result-build-image-native.log
