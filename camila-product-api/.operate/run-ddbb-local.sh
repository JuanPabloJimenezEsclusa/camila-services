#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")/.."

docker stop mongodb6 || true && \
  docker rm mongodb6 || true && \
  docker volume create mongo-data6 || true

docker run -it --rm \
  --network host \
  --name mongodb6 \
  -p 27017-27019:27017-27019 \
  --expose 27017-27019 \
  --memory="3072m" --memory-reservation="3072m" --memory-swap="3072m" --cpu-shares=2000 \
  -e MONGODB_INITDB_ROOT_USERNAME=camila \
  -e MONGODB_INITDB_ROOT_PASSWORD=camila \
  -v mongo-data6:/data/db \
  mongodb/mongodb-community-server:6.0-ubi8
