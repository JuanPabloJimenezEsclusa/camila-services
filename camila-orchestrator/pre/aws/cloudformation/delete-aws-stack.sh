#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

# Delete ecs stack
delete_ecs_stack() {
  echo "Init ${FUNCNAME:-} ..."

  aws cloudformation delete-stack \
    --no-cli-auto-prompt \
    --no-cli-pager \
    --stack-name "camila-product-stack"

  # Wait for ecs stack to be deleted
  echo "Waiting ${FUNCNAME:-} ..."
  aws cloudformation wait stack-delete-complete \
    --stack-name "camila-product-stack"

  echo "End ${FUNCNAME:-} successfully!"
}

# Delete secrets stack
delete_secrets_stack() {
  echo "Init ${FUNCNAME:-} ..."

  aws cloudformation delete-stack \
    --no-cli-auto-prompt \
    --no-cli-pager \
    --stack-name "camila-secrets-stack"

  # Wait for secrets stack to be deleted
  echo "Waiting ${FUNCNAME:-} ..."
  aws cloudformation wait stack-delete-complete \
    --stack-name "camila-secrets-stack"

  echo "End ${FUNCNAME:-} successfully!"
}

# Main script
main() {
  echo "Init ${0##*/} (${FUNCNAME:-})"

  echo -e "${SEPARATOR} 🗑️ Delete ecs stack. ${SEPARATOR}"
  delete_ecs_stack
  echo -e "${SEPARATOR} 🗑️ Delete secrets stack. ${SEPARATOR}"
  delete_secrets_stack

  echo "Done ${0##*/} (${FUNCNAME:-})"
}

time main
