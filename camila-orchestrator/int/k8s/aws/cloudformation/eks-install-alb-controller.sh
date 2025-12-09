#!/usr/bin/env bash

# Prerequisites:
# - aws cli installed
# - aws credentials configured
# - aws eks cluster created
# - kubectl installed
# - helm installed
# - openssl installed

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

__create_policy() {
  echo -e "${SEPARATOR} 🛠️ Create a policy if not exists. ${SEPARATOR}"
  aws iam create-policy \
    --policy-name AWSLoadBalancerControllerIAMPolicy \
    --policy-document file://API/iam-policy.json || true
}

__get_cluster_oidc_issuer() {
  echo -e "${SEPARATOR} 🛠️ Get cluster oidc issuer. ${SEPARATOR}"
  OIDC_URL="$(aws eks describe-cluster \
    --name camila-eks-cluster \
    --query "cluster.identity.oidc.issuer" --output text | sed 's|https://||')"
  echo "OIDC_URL: ${OIDC_URL}"
}

__fetch_certificate_chain() {
  echo -e "${SEPARATOR} 🛠️ Fetch the certificate chain from the OIDC provider. ${SEPARATOR}"
  OIDC_HOST="${OIDC_URL%/id/*}"
  echo "Fetching the certificate chain from: ${OIDC_HOST}"
  CERTIFICATE="$(echo | openssl s_client -connect "${OIDC_HOST}":443 2>/dev/null | openssl x509 -fingerprint -noout)"
  echo "Certificate: ${CERTIFICATE}"
}

__extract_sha1_fingerprint() {
  echo -e "${SEPARATOR} 🛠️ Extract the SHA1 fingerprint and format it. ${SEPARATOR}"
  THUMBPRINT=$(echo "${CERTIFICATE}" | sed -e 's/.*=//; s/://g' | tr '[:upper:]' '[:lower:]')
  echo "Thumbprint: ${THUMBPRINT}"
}

__create_oidc_provider() {
  echo -e "${SEPARATOR} 🛠️ Create the OIDC provider. ${SEPARATOR}"
  aws iam create-open-id-connect-provider \
    --url "https://${OIDC_URL}" \
    --client-id-list sts.amazonaws.com \
    --thumbprint-list "${THUMBPRINT}" || true
}

__replace_oidc_url() {
  echo -e "${SEPARATOR} 🛠️ Replace <oidc-issuer> with url in the file. ${SEPARATOR}"
  sed -i "s|<oidc-issuer>|${OIDC_URL}|g" "API/load-balancer-role-trust-policy.json"
}

__detach_role_and_policy() {
  echo -e "${SEPARATOR} 🛠️ Detach role and policy if exists. ${SEPARATOR}"
  aws iam detach-role-policy \
    --policy-arn arn:aws:iam::546053716955:policy/AWSLoadBalancerControllerIAMPolicy \
    --role-name AmazonEKSLoadBalancerControllerRole || true
}

__remove_role()  {
  echo -e "${SEPARATOR} 🛠️ Remove role if exists. ${SEPARATOR}"
  aws iam delete-role \
    --role-name AmazonEKSLoadBalancerControllerRole || true
}

__create_role() {
  echo -e "${SEPARATOR} 🛠️ Create a role. ${SEPARATOR}"
  aws iam create-role \
    --role-name AmazonEKSLoadBalancerControllerRole \
    --assume-role-policy-document file://"API/load-balancer-role-trust-policy.json"
}

__replace_oidc_issuer() {
  echo -e "${SEPARATOR} 🛠️ Replace <oidc-issuer> with url in the file (rollback). ${SEPARATOR}"
  sed -i "s|${OIDC_URL}|<oidc-issuer>|g" "API/load-balancer-role-trust-policy.json"
}

__attach_role_and_policy() {
  echo -e "${SEPARATOR} 🛠️ Attach role and policy. ${SEPARATOR}"
  aws iam attach-role-policy \
    --policy-arn arn:aws:iam::546053716955:policy/AWSLoadBalancerControllerIAMPolicy \
    --role-name AmazonEKSLoadBalancerControllerRole
}

__update_local_kubeconfig() {
  echo -e "${SEPARATOR} 🛠️ Update local kubeconfig. ${SEPARATOR}"
  aws eks update-kubeconfig --name camila-eks-cluster
}

__create_sa() {
  echo -e "${SEPARATOR} 🛠️ Create service account into EKS. ${SEPARATOR}"
  kubectl apply --filename API/aws-load-balancer-controller-service-account.yml
}

__get_vpc_id() {
  echo -e "${SEPARATOR} 🛠️ Get VPC ID. ${SEPARATOR}"
  vpcId="$(aws eks describe-cluster \
    --name camila-eks-cluster \
    --query "cluster.resourcesVpcConfig.vpcId" --output text)"
  echo "vpcId: ${vpcId}"
}

__install_load_balancer() {
  # Add and update ALB controller into HELM if not exists
  # https://github.com/aws/eks-charts/tree/master/stable/aws-load-balancer-controller
  echo -e "${SEPARATOR} 🛠️ Add and update ALB controller into HELM if not exists. ${SEPARATOR}"
  helm repo add eks https://aws.github.io/eks-charts || true
  helm repo update || true

  # Uninstall if exists
  echo -e "${SEPARATOR} 🛠️ Uninstall ALB controller if exists. ${SEPARATOR}"
  helm delete aws-load-balancer-controller -n kube-system || true
  helm uninstall aws-load-balancer-controller -n kube-system || true

  # Install ALB Controller in EKS
  echo -e "${SEPARATOR} 🛠️ Install ALB Controller in EKS. ${SEPARATOR}"
  helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
    -n kube-system \
    --set clusterName=camila-eks-cluster \
    --set serviceAccount.create=false \
    --set serviceAccount.name=aws-load-balancer-controller \
    --set vpcId="${vpcId}"
}

# Main function
main() {
  echo "Init ${0##*/} (${FUNCNAME:-})"

  __create_policy
  __get_cluster_oidc_issuer
  __fetch_certificate_chain
  __extract_sha1_fingerprint
  __create_oidc_provider
  __replace_oidc_url
  __detach_role_and_policy
  __remove_role
  __create_role
  __attach_role_and_policy
  __replace_oidc_issuer
  __update_local_kubeconfig
  __create_sa
  __get_vpc_id
  __install_load_balancer

  echo "Done ${0##*/} (${FUNCNAME:-})"
}

time main
