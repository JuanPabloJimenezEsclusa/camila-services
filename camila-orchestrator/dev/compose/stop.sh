#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

__stopServices() {
  echo -e "${SEPARATOR} 🔨 Stop services ${SEPARATOR}"
  docker-compose --file docker-compose.yml down --remove-orphans --volumes
}

__cleanEnvironment() {
  echo -e "${SEPARATOR} 🧹 Clean up ${SEPARATOR}"
  docker volume prune --force --all
  docker images --filter reference='camila-*' --format '{{.Repository}}:{{.Tag}}' | xargs -I {} docker rmi -f {}
}

main() {
  __stopServices
  __cleanEnvironment
}

echo -e "${SEPARATOR} 🔨 Main: ${0} ${SEPARATOR}"
time main
