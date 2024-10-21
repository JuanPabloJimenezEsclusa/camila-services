#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

cd "$(dirname "$0")"

delete_connector_provider() {
  echo "Init ${FUNCNAME:-} ..."

  # Get OpenIDConnectArn
  OpenIDConnectArn="$(aws iam list-open-id-connect-providers --query "OpenIDConnectProviderList[].Arn" --output text)"
  echo "OpenIDConnectArn: ${OpenIDConnectArn}"

  # Delete OpenIDConnectProvider
  aws iam delete-open-id-connect-provider \
    --open-id-connect-provider-arn "${OpenIDConnectArn}" || true

  echo "End ${FUNCNAME:-} successfully!"
}

delete_eks_stack() {
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

# Main script
main() {
  echo "Init ${0##*/} (${FUNCNAME:-})"
  delete_connector_provider
  delete_eks_stack
  echo "Done ${0##*/} (${FUNCNAME:-})"
}

time main
