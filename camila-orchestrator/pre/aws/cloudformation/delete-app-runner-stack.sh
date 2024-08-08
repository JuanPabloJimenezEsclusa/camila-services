#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")"

# Delete app runner stack
aws cloudformation delete-stack \
  --no-cli-auto-prompt \
  --no-cli-pager \
  --stack-name "camila-app-runner-stack"

# Wait for app runner stack to be deleted
aws cloudformation wait stack-delete-complete \
  --stack-name "camila-app-runner-stack"

echo "App Runner Stack deleted"

# Delete secrets stack
aws cloudformation delete-stack \
  --no-cli-auto-prompt \
  --no-cli-pager \
  --stack-name "camila-secrets-stack"

# Wait for secrets stack to be deleted
aws cloudformation wait stack-delete-complete \
  --stack-name "camila-secrets-stack"

echo "Secrets Stack deleted"
