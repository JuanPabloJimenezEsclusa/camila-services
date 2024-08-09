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
set -o xtrace

cd "$(dirname "$0")"

# Create a policy if not exists
aws iam create-policy \
  --policy-name AWSLoadBalancerControllerIAMPolicy \
  --policy-document file://API/iam-policy.json || true

# Get cluster oidc issuer
OIDC_URL="$(aws eks describe-cluster \
  --name camila-eks-cluster \
  --query "cluster.identity.oidc.issuer" --output text | sed 's|https://||')"
echo "OIDC_URL: ${OIDC_URL}"

# Fetch the certificate chain from the OIDC provider
echo "Fetching the certificate chain from: ${OIDC_URL}"
CERTIFICATE="$(echo | openssl s_client -connect "${OIDC_URL}":443 2>/dev/null | openssl x509 -fingerprint -noout)" # CAUTION!!
#CERTIFICATE="$(< API/_.eks.eu-west-1.amazonaws.com openssl x509 -fingerprint -noout)"
echo "Certificate: ${CERTIFICATE}"

# Extract the SHA1 fingerprint and format it
THUMBPRINT=$(echo "${CERTIFICATE}" | sed -e 's/.*=//; s/://g' | tr '[:upper:]' '[:lower:]')
echo "Thumbprint: ${THUMBPRINT}"

# Create the OIDC provider
aws iam create-open-id-connect-provider \
  --url "https://${OIDC_URL}" \
  --client-id-list sts.amazonaws.com \
  --thumbprint-list "${THUMBPRINT}"

# Replace <oidc-issuer> with url in the file
sed -i "s|<oidc-issuer>|${OIDC_URL}|g" "API/load-balancer-role-trust-policy.json"

# Des attach role and policy if exists
aws iam detach-role-policy \
  --policy-arn arn:aws:iam::546053716955:policy/AWSLoadBalancerControllerIAMPolicy \
  --role-name AmazonEKSLoadBalancerControllerRole || true

# Remove role if exists
aws iam delete-role \
  --role-name AmazonEKSLoadBalancerControllerRole || true

# Create a role
aws iam create-role \
  --role-name AmazonEKSLoadBalancerControllerRole \
  --assume-role-policy-document file://"API/load-balancer-role-trust-policy.json"

# Replace <oidc-issuer> with url in the file (rollback)
sed -i "s|${OIDC_URL}|<oidc-issuer>|g" "API/load-balancer-role-trust-policy.json"

# Attach role and policy
aws iam attach-role-policy \
  --policy-arn arn:aws:iam::546053716955:policy/AWSLoadBalancerControllerIAMPolicy \
  --role-name AmazonEKSLoadBalancerControllerRole

# Update local kubeconfig
aws eks update-kubeconfig --name camila-eks-cluster

# Create service account into EKS
kubectl apply --filename API/aws-load-balancer-controller-service-account.yml

# Add and update ALB controller into HELM if not exists
# https://github.com/aws/eks-charts/tree/master/stable/aws-load-balancer-controller
helm repo add eks https://aws.github.io/eks-charts || true
helm repo update || true

# Uninstall if exists
helm delete aws-load-balancer-controller -n kube-system || true
helm uninstall aws-load-balancer-controller -n kube-system || true

# Get VPC ID
vpcId="$(aws eks describe-cluster \
  --name camila-eks-cluster \
  --query "cluster.resourcesVpcConfig.vpcId" --output text)"
echo "vpcId: ${vpcId}"

# Install ALB Controller in EKS
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=camila-eks-cluster \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller \
  --set vpcId="${vpcId}"
