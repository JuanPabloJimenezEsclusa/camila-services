#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

# Create cognito oauth2 stack
create_cognito_oauth2_stack() {
  echo "Init ${FUNCNAME:-} ..."

  aws cloudformation create-stack \
    --stack-name "camila-cognito-oauth2-stack" \
    --template-body "file://templates/camila-cognito-oauth2-stack.yml" \
    --timeout-in-minutes 1 \
    --cli-read-timeout 30 \
    --cli-connect-timeout 30 \
    --capabilities CAPABILITY_NAMED_IAM

  # Wait for stack to be created
  echo "Waiting ${FUNCNAME:-} ..."
  aws cloudformation wait stack-create-complete \
    --stack-name "camila-cognito-oauth2-stack"

  echo "End ${FUNCNAME:-} successfully!"
}

# Set user password
set_user_password() {
  echo "Init ${FUNCNAME:-} ..."

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

  echo "End ${FUNCNAME:-} successfully!"
}

# Main script
main() {
  echo "Init ${0##*/} (${FUNCNAME:-})"

  echo -e "${SEPARATOR} 🔑 Create cognito oauth2 stack. ${SEPARATOR}"
  create_cognito_oauth2_stack
  echo -e "${SEPARATOR} 🔒 Set user password. ${SEPARATOR}"
  set_user_password

  echo "Done ${0##*/} (${FUNCNAME:-})"
}

time main
