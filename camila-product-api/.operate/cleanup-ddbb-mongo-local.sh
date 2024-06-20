#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")/.."

# Copy scripts into container
docker cp .operate/data/ mongodb7:/

# Execute script
docker exec -it mongodb7 \
  bash -c "mongosh -u camila -p camila < /data/cleanup_data.script"
