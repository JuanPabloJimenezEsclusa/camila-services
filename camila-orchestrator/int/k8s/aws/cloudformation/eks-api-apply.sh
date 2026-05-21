#!/usr/bin/env bash

# Prerequisites:
# - aws cli installed
# - aws credentials configured
# - aws eks cluster created
# - kubectl installed

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

ECR_REPOSITORY_NAME="${ECR_REPOSITORY_NAME:-camila-product-api}"
ECR_REGISTRY_ID="${ECR_REGISTRY_ID:-546053716955.dkr.ecr}"
ECR_REGION="${ECR_REGION:-eu-west-1}"
ECR_IMAGE_TAG="${ECR_IMAGE_TAG:-1.0.0}"

__build_project() {
  echo -e "${SEPARATOR} 🔨 Compile and build the project. ${SEPARATOR}"
  export SPRING_PROFILES_ACTIVE=int
  mvn --no-transfer-progress --also-make --batch-mode \
    spring-boot:build-image \
    -Dmaven.build.cache.enabled=false \
    -Dmaven.test.skip=true \
    -f ../../../../../camila-product-api/pom.xml
}

__create_ecr_repository() {
  echo -e "${SEPARATOR} 🛠️ Create ECR repository if not exists. ${SEPARATOR}"
  aws ecr describe-repositories --repository-names "${ECR_REPOSITORY_NAME}" >/dev/null 2>&1 || \
    aws ecr create-repository --repository-name "${ECR_REPOSITORY_NAME}"
}

__login_to_ecr() {
  echo -e "${SEPARATOR} 🔐 Login to ECR. ${SEPARATOR}"
  aws ecr get-login-password --region "${ECR_REGION}" | \
    docker login --username AWS --password-stdin "${ECR_REGISTRY_ID}.${ECR_REGION}.amazonaws.com"
}

__tag_and_push_image() {
  echo -e "${SEPARATOR} 📦 Tag and push the image to ECR. ${SEPARATOR}"
  docker tag "${ECR_REPOSITORY_NAME}:${ECR_IMAGE_TAG}" "${ECR_REGISTRY_ID}.${ECR_REGION}.amazonaws.com/${ECR_REPOSITORY_NAME}:${ECR_IMAGE_TAG}"
  docker push "${ECR_REGISTRY_ID}.${ECR_REGION}.amazonaws.com/${ECR_REPOSITORY_NAME}:${ECR_IMAGE_TAG}"
}

__set_mongodb_uri_secret() {
  echo -e "${SEPARATOR} 🛠️ Set MongoDB URI secret. ${SEPARATOR}"
  MONGO_URI_BASE64="$(echo "${MONGO_URI}" | tr -d '\n\r'  | base64 -w 0)"
  echo "MONGO_URI_BASE64: ${MONGO_URI_BASE64}"
  sed -i "s|<mongo-uri>|${MONGO_URI_BASE64}|g" "API/camila-product-api.yml"
}

__set_couchbase_password_secret() {
  echo -e "${SEPARATOR} 🛠️ Set Couchbase password secret. ${SEPARATOR}"
  COUCHBASE_PWD_BASE64="$(echo "${COUCHBASE_PASSWORD}" | tr -d '\n\r'  | base64 -w 0)"
  echo "COUCHBASE_PWD_BASE64: ${COUCHBASE_PWD_BASE64}"
  sed -i "s|<couchbase-password>|${COUCHBASE_PWD_BASE64}|g" "API/camila-product-api.yml"
}

__set_environment_variables() {
  echo -e "${SEPARATOR} 🛠️ Set environment variables. ${SEPARATOR}"
  sed -i "s|<couchbase-connection>|${COUCHBASE_CONNECTION}|g" "API/camila-product-api.yml"
  sed -i "s|<couchbase-username>|${COUCHBASE_USERNAME}|g" "API/camila-product-api.yml"
}

__get_certificate_arn() {
  echo -e "${SEPARATOR} 🛠️ Get certificate ARN. ${SEPARATOR}"
  CERTIFICATE_ARN="$(aws acm list-certificates \
    --query "CertificateSummaryList[].CertificateArn" \
    --output text)"
  echo "CERTIFICATE_ARN: ${CERTIFICATE_ARN}"
}

__update_alb_controller_template() {
  echo -e "${SEPARATOR} 🛠️ Update ALB controller with certificate ARN. ${SEPARATOR}"
  sed -i "s|<certificate-arn>|${CERTIFICATE_ARN}|g" "API/camila-product-api.yml"
}

__apply_api_templates() {
  echo -e "${SEPARATOR} 🛠️ Apply objects. ${SEPARATOR}"
  kubectl apply -f API/camila-product-api.yml
}

__rollback_api_templates() {
  echo -e "${SEPARATOR} 🛠️ Rollback changes to ALB controller template. ${SEPARATOR}"
  sed -i "s|${MONGO_URI_BASE64}|<mongo-uri>|g
          s|${COUCHBASE_PWD_BASE64}|<couchbase-password>|g
          s|${COUCHBASE_CONNECTION}|<couchbase-connection>|g
          s|${COUCHBASE_USERNAME}|<couchbase-username>|g
          s|${CERTIFICATE_ARN}|<certificate-arn>|g" "API/camila-product-api.yml"
}

__review_namespace() {
  echo -e "${SEPARATOR} 🛠️ Review namespace. ${SEPARATOR}"
  kubectl get all,resourcequotas,configmaps,secrets,ingress,hpa -n camila-product-api-ns -o wide --show-labels
}

__wait_for_deployment() {
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
}

__get_alb_dns_name() {
  echo -e "${SEPARATOR} 🛠️ Get ALB DNS name. ${SEPARATOR}"
  ALB_DNS_NAME="$(aws elbv2 describe-load-balancers \
    --names "camila-eks-cluster-lb" \
    --query "LoadBalancers[].DNSName" \
    --output text)"
  echo "ALB_DNS_NAME: ${ALB_DNS_NAME}"
}

__update_alb_controller_with_alb_dns_name() {
  echo -e "${SEPARATOR} 🛠️ Update ALB controller with ALB DNS name. ${SEPARATOR}"
  sed -i "s|<ALB_DNS_NAME>|${ALB_DNS_NAME}|g" "API/create-hosted-zone-record-sets.json"
}

__create_hosted_zone_record_sets() {
  echo -e "${SEPARATOR} 🛠️ Create a Hosted Zone record for the ALB. ${SEPARATOR}"
  aws route53 change-resource-record-sets \
    --hosted-zone-id Z05681561G3JHUOXZV5CN \
    --change-batch file://API/create-hosted-zone-record-sets.json
}

__update_alb_controller_with_original_alb_dns_name() {
  echo -e "${SEPARATOR} 🛠️ Update ALB controller with original ALB DNS name. ${SEPARATOR}"
  sed -i "s|${ALB_DNS_NAME}|<ALB_DNS_NAME>|g" "API/create-hosted-zone-record-sets.json"
}

# Main function
main() {
  echo "Init ${0##*/} (${FUNCNAME:-})"

  __build_project
  __create_ecr_repository
  __login_to_ecr
  __tag_and_push_image
  __set_mongodb_uri_secret
  __set_couchbase_password_secret
  __set_environment_variables
  __get_certificate_arn
  __update_alb_controller_template
  __apply_api_templates
  __rollback_api_templates
  __review_namespace
  __wait_for_deployment
  __get_alb_dns_name
  __update_alb_controller_with_alb_dns_name
  __create_hosted_zone_record_sets
  __update_alb_controller_with_original_alb_dns_name

  echo "Done ${0##*/} (${FUNCNAME:-})"
}

time main
