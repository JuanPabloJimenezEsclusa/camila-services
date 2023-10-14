#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")/.."

# iniciar contenedor
docker run --rm -it \
  --name="camila-gateway" \
  --network=host \
  --env SPRING_PROFILES_ACTIVE=loc \
  --memory="1024m" --memory-reservation="1024m" --memory-swap="1024m" --cpu-shares=2000 \
  docker.io/library/camila-config:1.0.0
