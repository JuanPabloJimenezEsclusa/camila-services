#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")"

# Create stack
aws cloudformation create-stack \
  --stack-name "camila-cognito-oauth2-stack" \
  --template-body "file://templates/camila-cognito-oauth2-stack.yml" \
  --timeout-in-minutes 1 \
  --cli-read-timeout 30 \
  --cli-connect-timeout 30 \
  --capabilities CAPABILITY_NAMED_IAM

# Wait for stack to be created
aws cloudformation wait stack-create-complete \
  --stack-name "camila-cognito-oauth2-stack"
echo "Stack created"

# Get user pool id
USER_POOL_ID="$(aws cloudformation describe-stacks \
  --stack-name "camila-cognito-oauth2-stack" \
  --query "Stacks[0].Outputs[?OutputKey=='UserPoolId'].OutputValue" --output text)"

#  Set password for user
aws cognito-idp admin-set-user-password \
  --username olbapnuaj@gmail.com \
  --password camila \
  --user-pool-id "${USER_POOL_ID}" \
  --permanent
echo "Password set"
