#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

cd "$(dirname "$0")/.."

# compilar nativo
export SPRING_PROFILES_ACTIVE=loc

mvn clean package -Pnative
mvn spring-boot:build-image -Pnative -Dmaven.test.skip=true -f ./pom.xml
