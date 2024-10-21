#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

cd "$(dirname "$0")/.."

# Init container
docker run --rm -it \
  --name="camila-product-api" \
  --network=host \
  --env SPRING_PROFILES_ACTIVE=loc \
  --env LANG=en_US.utf8 \
  --env LANGUAGE=en_US.utf8 \
  --env LC_ALL=en_US.utf8 \
  --memory="1024m" --memory-reservation="1024m" --memory-swap="1024m" --cpu-shares=2000 \
  docker.io/library/camila-product-api:1.0.0
