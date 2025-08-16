#!/usr/bin/env bash

# Prerequisites:
# - aws eks cluster created
# - kubectl installed

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

__cleanup() {
  echo -e "${SEPARATOR} 🗑️ Delete namespace. ${SEPARATOR}"
  kubectl delete namespaces camila-product-api-ns --grace-period=0 --force
}

#Main function
main() {
  echo "Init ${0##*/} (${FUNCNAME:-})"

  __cleanup

  echo "Done ${0##*/} (${FUNCNAME:-})"
}

time main
