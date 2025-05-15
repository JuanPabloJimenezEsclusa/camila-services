#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

workspace="$(pwd)"
baseProjectPath="../../../"

# Environment variables
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-"dev"}"
export GRAALVM_HOME="${GRAALVM_HOME:-"/usr/lib/jvm/graalvm-jdk-21.0.1+12.1"}"

__buildProjects() {
  # root project workspace path
  cd "${workspace}/${baseProjectPath}"
  # compile and build the project
  mvn clean spring-boot:build-image -Dmaven.test.skip=true -Dmaven.build.cache.enabled=false -f ./pom.xml
}

__initServices() {
  cd "${workspace}"
  # init services
  docker-compose --file docker-compose.yml up -d --build --force-recreate
  # show services status
  docker-compose --file docker-compose.yml ps
}

main() {
  __buildProjects
  __initServices
}

echo -e "${SEPARATOR} 🔨 Main: ${0} ${SEPARATOR}"
time main | tee result-dev-start.log
