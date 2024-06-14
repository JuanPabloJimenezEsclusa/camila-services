#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")"

# Delete stack
aws cloudformation delete-stack \
  --no-cli-auto-prompt \
  --no-cli-pager \
  --stack-name "camila-cognito-oauth2-stack"

# Wait for stack to be deleted
aws cloudformation wait stack-delete-complete \
  --stack-name "camila-cognito-oauth2-stack"

echo "Stack deleted"
