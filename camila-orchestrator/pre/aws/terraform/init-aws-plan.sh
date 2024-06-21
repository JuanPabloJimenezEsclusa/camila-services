#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR

cd "$(dirname "$0")"

COUCHBASE_CONNECTION="${COUCHBASE_CONNECTION:-}"
COUCHBASE_USERNAME="${COUCHBASE_USERNAME:-}"
COUCHBASE_PASSWORD="${COUCHBASE_PASSWORD:-}"
MONGO_URI="${MONGO_URI:-}"

# Function to validate environment variables
validate_env_var() {
  local var_name="${1}"
  local var_value="${2}"

  if [ -z "$var_value" ]; then
    echo "Error: ${var_name} environment variable is empty."
    exit 1
  fi
}

# Set log level to DEBUG
export TF_LOG="INFO"

# Init Terraform and install providers
terraform -chdir=templates init

# Validate required environment variables
terraform -chdir=templates validate

# Run Terraform plan
validate_env_var "COUCHBASE_CONNECTION" "${COUCHBASE_CONNECTION}"
validate_env_var "COUCHBASE_USERNAME" "${COUCHBASE_USERNAME}"
validate_env_var "COUCHBASE_PASSWORD" "${COUCHBASE_PASSWORD}"
validate_env_var "MONGO_URI" "${MONGO_URI}"

terraform -chdir=templates plan -compact-warnings -out=tfplan \
  -var "couchbase_connection=${COUCHBASE_CONNECTION}" \
  -var "couchbase_username=${COUCHBASE_USERNAME}" \
  -var "couchbase_password=${COUCHBASE_PASSWORD}" \
  -var "mongo_uri=${MONGO_URI}"
