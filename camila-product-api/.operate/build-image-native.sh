#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

cd "$(dirname "$0")/../"

# Native build
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-"loc"}"
export GRAALVM_HOME="${GRAALVM_HOME:-"/usr/lib/jvm/graalvm-jdk-25+37.1"}"

# Compile/package and build container image
echo "Building native image container..."
mvn clean spring-boot:build-image \
  -Pnative \
  -Dmaven.test.skip=true \
  -Dspring-boot.aot.jvmArguments="--add-opens=java.base/java.lang=ALL-UNNAMED" \
  -f ./camila-product-api-infrastructure/driving/camila-product-api-infrastructure-boot/pom.xml | tee result-build-image-native.log

if [[ ${PIPESTATUS[0]} -ne 0 ]]; then
  echo "ERROR: Native image container build failed. See 'result-build-image-native.log' for details."
  exit 1
fi

echo "Native image container build completed successfully."
