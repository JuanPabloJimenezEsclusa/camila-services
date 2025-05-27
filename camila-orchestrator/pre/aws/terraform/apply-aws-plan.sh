#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

# Main script
main() {
  echo "Init ${0##*/} (${FUNCNAME:-})"

  # Create the terraform plan
  echo -e "${SEPARATOR} 📝 Run Terraform plan. ${SEPARATOR}"
  terraform -chdir=templates apply -auto-approve tfplan

  echo "Done ${0##*/} (${FUNCNAME:-})"
}

time main
