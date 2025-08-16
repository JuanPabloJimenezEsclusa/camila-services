#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

__install_aws_cli() {
  echo "Init ${FUNCNAME:-} ..."

  curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
  unzip awscliv2.zip
  sudo ./aws/install --update
  rm -rf awscliv2.zip aws
  aws --version

  echo "End ${FUNCNAME:-} successfully!"
}

__install_k9s() {
  echo "Init ${FUNCNAME:-} ..."

  wget https://github.com/derailed/k9s/releases/latest/download/k9s_linux_amd64.deb \
    && sudo apt install ./k9s_linux_amd64.deb \
    && rm k9s_linux_amd64.deb || true
  k9s version

  echo "End ${FUNCNAME:-} successfully!"
}

__create_eks_stack() {
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

# Main function
main() {
  echo "Init ${0##*/} (${FUNCNAME:-})"

  echo -e "${SEPARATOR} 🛠️ Install aws cli. ${SEPARATOR}"
  __install_aws_cli
  echo -e "${SEPARATOR} 🛠️ Install k9s. ${SEPARATOR}"
  __install_k9s
  echo -e "${SEPARATOR} 🛠️ Create eks stack. ${SEPARATOR}"
  __create_eks_stack

  echo "Done ${0##*/} (${FUNCNAME:-})"
}

time main
