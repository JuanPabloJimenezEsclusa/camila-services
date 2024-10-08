#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")"

# Create eks stack
aws cloudformation create-stack \
  --stack-name "camila-eks-stack" \
  --template-body "file://templates/eks.yml" \
  --capabilities CAPABILITY_NAMED_IAM \
  --parameters

# Wait for stack to be created
aws cloudformation wait stack-create-complete \
  --stack-name "camila-eks-stack"
