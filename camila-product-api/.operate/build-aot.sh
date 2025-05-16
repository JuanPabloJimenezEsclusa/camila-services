#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

cd "$(dirname "$0")/.."

# AOT compile
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-"loc"}"

mvn clean compile spring-boot:process-aot package \
  -Dmaven.build.cache.enabled=false \
  -Dspring-boot.run.jvmArguments="--add-opens=java.base/java.lang=ALL-UNNAMED" \
  -f ./pom.xml
