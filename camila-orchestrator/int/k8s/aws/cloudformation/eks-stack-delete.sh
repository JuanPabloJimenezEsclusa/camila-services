#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")"

# Get OpenIDConnectArn
OpenIDConnectArn="$(aws iam list-open-id-connect-providers --query "OpenIDConnectProviderList[].Arn" --output text)"
echo "OpenIDConnectArn: ${OpenIDConnectArn}"

# Delete OpenIDConnectProvider
aws iam delete-open-id-connect-provider \
  --open-id-connect-provider-arn "${OpenIDConnectArn}" || true
echo "OpenIDConnectProvider deleted"

# Delete eks stack
aws cloudformation delete-stack \
  --no-cli-auto-prompt \
  --no-cli-pager \
  --stack-name "camila-eks-stack"

# Wait for eks stack to be deleted
aws cloudformation wait stack-delete-complete \
  --stack-name "camila-eks-stack"

echo "EKS Stack deleted"
