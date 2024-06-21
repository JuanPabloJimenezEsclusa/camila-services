#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")"

COUCHBASE_CONNECTION="${COUCHBASE_CONNECTION:-}"
COUCHBASE_USERNAME="${COUCHBASE_USERNAME:-}"
COUCHBASE_PASSWORD="${COUCHBASE_PASSWORD:-}"
MONGO_URI="${MONGO_URI:-}"

# Create the terraform plan
terraform -chdir=templates apply -destroy -auto-approve \
  -var "couchbase_connection=${COUCHBASE_CONNECTION}" \
  -var "couchbase_username=${COUCHBASE_USERNAME}" \
  -var "couchbase_password=${COUCHBASE_PASSWORD}" \
  -var "mongo_uri=${MONGO_URI}"
