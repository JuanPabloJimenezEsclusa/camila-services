#!/usr/bin/env bash

# Prerequisites:
# - aws cli installed
# - aws credentials configured
# - aws eks cluster created
# - kubectl installed

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")"

# Set MongoDB URI secret
MONGO_URI_BASE64="$(echo "${MONGO_URI}" | tr -d '\n\r'  | base64 -w 0)"
echo "MONGO_URI_BASE64: ${MONGO_URI_BASE64}"
sed -i "s|<mongo-uri>|${MONGO_URI_BASE64}|g" "API/camila-product-api.yml"

# Set Couchbase password secret
COUCHBASE_PWD_BASE64="$(echo "${COUCHBASE_PASSWORD}" | tr -d '\n\r'  | base64 -w 0)"
echo "COUCHBASE_PWD_BASE64: ${COUCHBASE_PWD_BASE64}"
sed -i "s|<couchbase-password>|${COUCHBASE_PWD_BASE64}|g" "API/camila-product-api.yml"

# Set environment variables
sed -i "s|<couchbase-connection>|${COUCHBASE_CONNECTION}|g" "API/camila-product-api.yml"
sed -i "s|<couchbase-username>|${COUCHBASE_USERNAME}|g" "API/camila-product-api.yml"

# Get certificate ARN
CERTIFICATE_ARN="$(aws acm list-certificates \
  --query "CertificateSummaryList[].CertificateArn" \
  --output text)"
echo "CERTIFICATE_ARN: ${CERTIFICATE_ARN}"

# Update ALB controller with certificate ARN
sed -i "s|<certificate-arn>|${CERTIFICATE_ARN}|g" "API/camila-product-api.yml"

# Apply objects
kubectl apply -f API/camila-product-api.yml

# Rollback changes to ALB controller template
sed -i "s|${MONGO_URI_BASE64}|<mongo-uri>|g
        s|${COUCHBASE_PWD_BASE64}|<couchbase-password>|g
        s|${COUCHBASE_CONNECTION}|<couchbase-connection>|g
        s|${COUCHBASE_USERNAME}|<couchbase-username>|g
        s|${CERTIFICATE_ARN}|<certificate-arn>|g" "API/camila-product-api.yml"

# Review namespace
kubectl get all,resourcequotas,configmaps,secrets,ingress,hpa -n camila-product-api-ns -o wide --show-labels

# Wait for ALB to be active
ALB_STATE="empty"
while [ "${ALB_STATE}" != "active" ]; do
  ALB_STATE="$(aws elbv2 describe-load-balancers \
    --names "camila-eks-cluster-lb" \
    --query "LoadBalancers[].State.Code" \
    --output text 2>/dev/null || echo "empty")"
  echo "Waiting for ALB to be active...  ${ALB_STATE}"
  sleep 5
done

# Get ALB DNS name
ALB_DNS_NAME="$(aws elbv2 describe-load-balancers \
  --names "camila-eks-cluster-lb" \
  --query "LoadBalancers[].DNSName" \
  --output text)"
echo "ALB_DNS_NAME: ${ALB_DNS_NAME}"

# Update ALB controller with ALB DNS name
sed -i "s|<ALB_DNS_NAME>|${ALB_DNS_NAME}|g" "API/create-hosted-zone-record-sets.json"

# Create a Hosted Zone record for the ALB
aws route53 change-resource-record-sets \
  --hosted-zone-id Z102528520PCT47CK313B \
  --change-batch file://API/create-hosted-zone-record-sets.json

# Update ALB controller with original ALB DNS name
sed -i "s|${ALB_DNS_NAME}|<ALB_DNS_NAME>|g" "API/create-hosted-zone-record-sets.json"
