#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

__delete_connector_provider() {
  echo "Init ${FUNCNAME:-} ..."

  # Get OpenIDConnectArn
  OpenIDConnectArn="$(aws iam list-open-id-connect-providers --query "OpenIDConnectProviderList[].Arn" --output text)"
  echo "OpenIDConnectArn: ${OpenIDConnectArn}"

  # Delete OpenIDConnectProvider
  aws iam delete-open-id-connect-provider \
    --open-id-connect-provider-arn "${OpenIDConnectArn}" || true

  echo "End ${FUNCNAME:-} successfully!"
}

__delete_eks_stack() {
  echo "Init ${FUNCNAME:-} ..."

  # Delete eks stack
  aws cloudformation delete-stack \
    --no-cli-auto-prompt \
    --no-cli-pager \
    --stack-name "camila-eks-stack"

  # Wait for eks stack to be deleted
  echo "Waiting ${FUNCNAME:-} ..."
  aws cloudformation wait stack-delete-complete \
    --stack-name "camila-eks-stack"

  echo "End ${FUNCNAME:-} successfully!"
}

# Main function
main() {
  echo "Init ${0##*/} (${FUNCNAME:-})"

  echo -e "${SEPARATOR} 🗑️ Delete connector provider. ${SEPARATOR}"
  __delete_connector_provider
  echo -e "${SEPARATOR}  🗑️ Delete eks stack. ${SEPARATOR}"
  __delete_eks_stack

  echo "Done ${0##*/} (${FUNCNAME:-})"
}

time main
