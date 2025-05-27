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

# Create a policy if not exists
echo -e "${SEPARATOR} 🛠️ Create a policy if not exists. ${SEPARATOR}"
aws iam create-policy \
  --policy-name AWSLoadBalancerControllerIAMPolicy \
  --policy-document file://API/iam-policy.json || true

# Get cluster oidc issuer
echo -e "${SEPARATOR} 🛠️ Get cluster oidc issuer. ${SEPARATOR}"
OIDC_URL="$(aws eks describe-cluster \
  --name camila-eks-cluster \
  --query "cluster.identity.oidc.issuer" --output text | sed 's|https://||')"
echo "OIDC_URL: ${OIDC_URL}"

# Fetch the certificate chain from the OIDC provider
echo -e "${SEPARATOR} 🛠️ Fetch the certificate chain from the OIDC provider. ${SEPARATOR}"
OIDC_HOST="${OIDC_URL%/id/*}"
echo "Fetching the certificate chain from: ${OIDC_HOST}"
CERTIFICATE="$(echo | openssl s_client -connect "${OIDC_HOST}":443 2>/dev/null | openssl x509 -fingerprint -noout)"
echo "Certificate: ${CERTIFICATE}"

# Extract the SHA1 fingerprint and format it
echo -e "${SEPARATOR} 🛠️ Extract the SHA1 fingerprint and format it. ${SEPARATOR}"
THUMBPRINT=$(echo "${CERTIFICATE}" | sed -e 's/.*=//; s/://g' | tr '[:upper:]' '[:lower:]')
echo "Thumbprint: ${THUMBPRINT}"

# Create the OIDC provider
echo -e "${SEPARATOR} 🛠️ Create the OIDC provider. ${SEPARATOR}"
aws iam create-open-id-connect-provider \
  --url "https://${OIDC_URL}" \
  --client-id-list sts.amazonaws.com \
  --thumbprint-list "${THUMBPRINT}"

# Replace <oidc-issuer> with url in the file
echo -e "${SEPARATOR} 🛠️ Replace <oidc-issuer> with url in the file. ${SEPARATOR}"
sed -i "s|<oidc-issuer>|${OIDC_URL}|g" "API/load-balancer-role-trust-policy.json"

# Detach role and policy if exists
echo -e "${SEPARATOR} 🛠️ Detach role and policy if exists. ${SEPARATOR}"
aws iam detach-role-policy \
  --policy-arn arn:aws:iam::546053716955:policy/AWSLoadBalancerControllerIAMPolicy \
  --role-name AmazonEKSLoadBalancerControllerRole || true

# Remove role if exists
echo -e "${SEPARATOR} 🛠️ Remove role if exists. ${SEPARATOR}"
aws iam delete-role \
  --role-name AmazonEKSLoadBalancerControllerRole || true

# Create a role
echo -e "${SEPARATOR} 🛠️ Create a role. ${SEPARATOR}"
aws iam create-role \
  --role-name AmazonEKSLoadBalancerControllerRole \
  --assume-role-policy-document file://"API/load-balancer-role-trust-policy.json"

# Replace <oidc-issuer> with url in the file (rollback)
echo -e "${SEPARATOR} 🛠️ Replace <oidc-issuer> with url in the file (rollback). ${SEPARATOR}"
sed -i "s|${OIDC_URL}|<oidc-issuer>|g" "API/load-balancer-role-trust-policy.json"

# Attach role and policy
echo -e "${SEPARATOR} 🛠️ Attach role and policy. ${SEPARATOR}"
aws iam attach-role-policy \
  --policy-arn arn:aws:iam::546053716955:policy/AWSLoadBalancerControllerIAMPolicy \
  --role-name AmazonEKSLoadBalancerControllerRole

# Update local kubeconfig
echo -e "${SEPARATOR} 🛠️ Update local kubeconfig. ${SEPARATOR}"
aws eks update-kubeconfig --name camila-eks-cluster

# Create service account into EKS
echo -e "${SEPARATOR} 🛠️ Create service account into EKS. ${SEPARATOR}"
kubectl apply --filename API/aws-load-balancer-controller-service-account.yml

# Add and update ALB controller into HELM if not exists
# https://github.com/aws/eks-charts/tree/master/stable/aws-load-balancer-controller
echo -e "${SEPARATOR} 🛠️ Add and update ALB controller into HELM if not exists. ${SEPARATOR}"
helm repo add eks https://aws.github.io/eks-charts || true
helm repo update || true

# Uninstall if exists
echo -e "${SEPARATOR} 🛠️ Uninstall ALB controller if exists. ${SEPARATOR}"
helm delete aws-load-balancer-controller -n kube-system || true
helm uninstall aws-load-balancer-controller -n kube-system || true

# Get VPC ID
echo -e "${SEPARATOR} 🛠️ Get VPC ID. ${SEPARATOR}"
vpcId="$(aws eks describe-cluster \
  --name camila-eks-cluster \
  --query "cluster.resourcesVpcConfig.vpcId" --output text)"
echo "vpcId: ${vpcId}"

# Install ALB Controller in EKS
echo -e "${SEPARATOR} 🛠️ Install ALB Controller in EKS. ${SEPARATOR}"
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=camila-eks-cluster \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller \
  --set vpcId="${vpcId}"
