#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

# Create eks stack
create_eks_stack() {
  echo "Init ${FUNCNAME:-} ..."

  aws cloudformation create-stack \
    --stack-name "camila-eks-stack" \
    --template-body "file://templates/eks.yml" \
    --capabilities CAPABILITY_NAMED_IAM \
    --parameters

  # Wait for stack to be created
  echo "Waiting ${FUNCNAME:-} ..."
  aws cloudformation wait stack-create-complete \
    --stack-name "camila-eks-stack"

  echo "End ${FUNCNAME:-} successfully!"
}

# Main script
main() {
  echo "Init ${0##*/} (${FUNCNAME:-})"
  echo -e "${SEPARATOR} 🛠️ Create eks stack. ${SEPARATOR}"
  create_eks_stack
  echo "Done ${0##*/} (${FUNCNAME:-})"
}

time main
