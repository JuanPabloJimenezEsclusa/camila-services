#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

cd "$(dirname "$0")/.."

# Prepare workspace
docker stop mongodb7 || true && \
  docker rm mongodb7 || true && \
  docker volume create mongo-data7 || true

# Init container
docker run -it --rm \
  --network host \
  --name mongodb7 \
  -p 27017-27019:27017-27019 \
  --expose 27017-27019 \
  --oom-kill-disable \
  --memory="6192m" --memory-reservation="4096m" --memory-swap="6192m" --cpu-shares=4000 \
  -e MONGODB_INITDB_ROOT_USERNAME=camila \
  -e MONGODB_INITDB_ROOT_PASSWORD=camila \
  -v mongo-data7:/data/db \
  mongodb/mongodb-community-server:7.0.8-ubi8
