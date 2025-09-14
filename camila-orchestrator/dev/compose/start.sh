#!/usr/bin/env bash

# Example of usage: ./start.sh buildProjects=true

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

parameter="${1:-"buildProjects=false"}"
eval "${parameter}"
echo "buildProjects: ${buildProjects:-}"

workspace="$(pwd)"
baseProjectPath="../../../"

# Environment variables
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-"dev"}"
export GRAALVM_HOME="${GRAALVM_HOME:-"/usr/lib/jvm/graalvm-jdk-24.0.1+9.1"}"

__buildProjects() {
  if [[ "${buildProjects:-}" == "true" ]]; then
    echo -e "${SEPARATOR} 🔨 Compile and build the image. ${SEPARATOR}"
    # root project workspace path
    cd "${workspace}/${baseProjectPath}"
    # compile and build the project
    mvn clean spring-boot:build-image \
      -Dmaven.test.skip=true \
      -Dmaven.build.cache.enabled=false \
      --projects camila-admin,camila-config,camila-discovery,camila-gateway,camila-product-api \
      -f ./pom.xml
  else
    echo -e "🚧 Skip build projects"
  fi
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
