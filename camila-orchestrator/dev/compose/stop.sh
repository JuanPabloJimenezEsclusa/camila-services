#!/usr/bin/env bash

# Example of usage: ./stop.sh removeImages=true

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

parameter="${1:-"removeImages=false"}"
eval "${parameter}"
echo "removeImages: ${removeImages:-}"

__stopServices() {
  echo -e "${SEPARATOR} 🔨 Stop services ${SEPARATOR}"
  docker-compose --file docker-compose.yml down --remove-orphans --volumes || true
}

__cleanEnvironment() {
  echo -e "${SEPARATOR} 🧹 Clean up ${SEPARATOR}"
  docker volume prune --force --all

  if [[ "${removeImages:-}" == "true" ]]; then
    docker images --filter reference='camila-*' --format '{{.Repository}}:{{.Tag}}' | xargs -I {} docker rmi -f {}
  else
    echo -e "🚧 Skip remove images"
  fi
}

main() {
  __stopServices
  __cleanEnvironment
}

echo -e "${SEPARATOR} 🔨 Main: ${0} ${SEPARATOR}"
time main
