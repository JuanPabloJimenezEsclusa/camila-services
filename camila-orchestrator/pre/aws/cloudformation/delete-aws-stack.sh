#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")"

# Delete ecs stack
aws cloudformation delete-stack \
  --no-cli-auto-prompt \
  --no-cli-pager \
  --stack-name "camila-product-stack"

# Wait for ecs stack to be deleted
aws cloudformation wait stack-delete-complete \
  --stack-name "camila-product-stack"

echo "ECS Stack deleted"

# Delete secrets stack
aws cloudformation delete-stack \
  --no-cli-auto-prompt \
  --no-cli-pager \
  --stack-name "camila-secrets-stack"

# Wait for secrets stack to be deleted
aws cloudformation wait stack-delete-complete \
  --stack-name "camila-secrets-stack"

echo "Secrets Stack deleted"
