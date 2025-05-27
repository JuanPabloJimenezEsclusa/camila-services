#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

mongoUser="${1:-camila}"
mongoPassword="${2:-camila}"
mongoShellScript="mongosh -u ${mongoUser} -p ${mongoPassword} < /data/mongo/cleanup_data.script"

cd "$(dirname "$0")/.."

# Copy scripts into container
docker cp .operate/data/ mongodb:/

# Execute script
docker exec -it mongodb bash -c "${mongoShellScript}"
