#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")/.."

# ejecutar con aot
export SPRING_PROFILES_ACTIVE=loc
java -Dspring.aot.enabled=true \
  -jar target/camila-config-1.0.0.jar
