#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

cd "$(dirname "$0")/.."

# Prepare workspace
docker stop couchbase7 || true && \
  docker rm couchbase7 || true && \
  docker volume create couchbase-data7 || true

# Init container
docker run -it --rm \
  --network host \
  --name couchbase7 \
  -p 8091-8096 \
  -p 11210-11211 \
  --expose 8091-8096 \
  --expose 11210-11211 \
  --ulimit core=100000000:100000000 \
  --ulimit memlock=100000000:100000000 \
  --oom-kill-disable \
  -e CLUSTER_NAME=camila-couchbase-cluster \
  -e COUCHBASE_ADMINISTRATOR_USERNAME=admin \
  -e COUCHBASE_ADMINISTRATOR_PASSWORD=admin1234 \
  -e COUCHBASE_BUCKET=camila-product-bucket \
  -e COUCHBASE_BUCKET_SCOPE=product \
  -e COUCHBASE_BUCKET_COLLECTION=products \
  -e COUCHBASE_BUCKET_RAMSIZE=2048 \
  -e COUCHBASE_RAM_SIZE=2048 \
  -e COUCHBASE_INDEX_RAM_SIZE=1024 \
  --memory="4096m" --memory-reservation="4096m" --memory-swap="4096m" --cpu-shares=4000 \
  -v ./.operate/data/couchbase:/opt/couchbase/init \
  -v couchbase-data7:/opt/couchbase/var \
  couchbase/server:community-7.6.3 sh -c "/opt/couchbase/init/init-cbserver.sh"
