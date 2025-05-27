#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

COUCHBASE_CONNECTION="${COUCHBASE_CONNECTION:-}"
COUCHBASE_USERNAME="${COUCHBASE_USERNAME:-}"
COUCHBASE_PASSWORD="${COUCHBASE_PASSWORD:-}"
MONGO_URI="${MONGO_URI:-}"

# Main script
main() {
  echo "Init ${0##*/} (${FUNCNAME:-})"

  # Destroy the terraform plan
  echo -e "${SEPARATOR} 🗑️ Destroy the terraform plan. ${SEPARATOR}"
  terraform -chdir=templates apply -destroy -auto-approve \
    -var "couchbase_connection=${COUCHBASE_CONNECTION}" \
    -var "couchbase_username=${COUCHBASE_USERNAME}" \
    -var "couchbase_password=${COUCHBASE_PASSWORD}" \
    -var "mongo_uri=${MONGO_URI}"

  echo "Done ${0##*/} (${FUNCNAME:-})"
}

time main
