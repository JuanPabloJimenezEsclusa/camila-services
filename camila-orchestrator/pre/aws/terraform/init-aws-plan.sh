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

# Function to validate environment variables
validate_env_var() {
  local var_name="${1}"
  local var_value="${2}"

  if [ -z "$var_value" ]; then
    echo "Error: ${var_name} environment variable is empty."
    exit 1
  fi
}

# Main script
main() {
  echo "Init ${0##*/} (${FUNCNAME:-})"

  # Set log level to DEBUG
  echo -e "${SEPARATOR} 🛠️ Set log level to DEBUG. ${SEPARATOR}"
  export TF_LOG="INFO"

  # Init Terraform and install providers
  echo -e "${SEPARATOR} 🚀 Init Terraform and install providers. ${SEPARATOR}"
  terraform -chdir=templates init -upgrade

  # Validate required environment variables
  echo -e "${SEPARATOR} 🧪 Validate required environment variables. ${SEPARATOR}"
  terraform -chdir=templates validate

  validate_env_var "COUCHBASE_CONNECTION" "${COUCHBASE_CONNECTION}"
  validate_env_var "COUCHBASE_USERNAME" "${COUCHBASE_USERNAME}"
  validate_env_var "COUCHBASE_PASSWORD" "${COUCHBASE_PASSWORD}"
  validate_env_var "MONGO_URI" "${MONGO_URI}"

  # Run Terraform plan
  echo -e "${SEPARATOR} 📝 Run Terraform plan. ${SEPARATOR}"
  terraform -chdir=templates plan -compact-warnings -out=tfplan \
    -var "couchbase_connection=${COUCHBASE_CONNECTION}" \
    -var "couchbase_username=${COUCHBASE_USERNAME}" \
    -var "couchbase_password=${COUCHBASE_PASSWORD}" \
    -var "mongo_uri=${MONGO_URI}"

  echo "Done ${0##*/} (${FUNCNAME:-})"
}

time main
