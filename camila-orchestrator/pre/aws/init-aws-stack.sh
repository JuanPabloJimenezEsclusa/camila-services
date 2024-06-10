#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")"

GITHUB_ECR_REGISTRY_ID="${GITHUB_ECR_REGISTRY_ID:-ghcr.io/juanpablojimenezesclusa}"
ECR_REPOSITORY_NAME="${ECR_REPOSITORY_NAME:-camila-product-api}"
ECR_REGISTRY_ID="${ECR_REGISTRY_ID:-546053716955.dkr.ecr}"
ECR_REGION="${ECR_REGION:-eu-west-1}"
ECR_IMAGE_TAG="${ECR_IMAGE_TAG:-1.0.0}"

COUCHBASE_CONNECTION="${COUCHBASE_CONNECTION:-}"
COUCHBASE_USERNAME="${COUCHBASE_USERNAME:-}"
COUCHBASE_PASSWORD="${COUCHBASE_PASSWORD:-}"
MONGO_URI="${MONGO_URI:-}"

# Function to validate environment variables
validate_env_var() {
  local var_name="${1}"
  local var_value="${2}"

  if [ -z "$var_value" ]; then
    echo "Error: ${var_name} environment variable is empty."
    exit 1
  fi
}

# Function to validate URL format (basic check)
validate_url_format() {
  local url="$1"
  if ! [[ "$url" =~ ^(couchbases|mongodb\+srv):// ]]; then
    echo "Error: Invalid URL format for $url."
    exit 1
  fi
}

# Create ECR repository if it doesn't exist
create_ecr_repository() {
  aws ecr describe-repositories --repository-names "${ECR_REPOSITORY_NAME}" >/dev/null 2>&1 || \
    aws ecr create-repository --repository-name "${ECR_REPOSITORY_NAME}"
}

# Login to ECR
login_to_ecr() {
  aws ecr get-login-password --region "${ECR_REGION}" | \
    docker login --username AWS --password-stdin "${ECR_REGISTRY_ID}.${ECR_REGION}.amazonaws.com"
}

# Build and push Docker image to ECR
build_and_push_image() {
  # Check if the Docker image exists locally
  if ! docker inspect "${ECR_REPOSITORY_NAME}:${ECR_IMAGE_TAG}" >/dev/null 2>&1; then
    # If the image doesn't exist locally, try to pull it from the GitHub package
    docker pull "${GITHUB_ECR_REGISTRY_ID}/${ECR_REPOSITORY_NAME}:${ECR_IMAGE_TAG}"
    docker tag "${GITHUB_ECR_REGISTRY_ID}/${ECR_REPOSITORY_NAME}:${ECR_IMAGE_TAG}" "${ECR_REPOSITORY_NAME}:${ECR_IMAGE_TAG}"
  fi

  # Build and tag image and push to ECR
  docker tag "${ECR_REPOSITORY_NAME}:${ECR_IMAGE_TAG}" "${ECR_REGISTRY_ID}.${ECR_REGION}.amazonaws.com/${ECR_REPOSITORY_NAME}:${ECR_IMAGE_TAG}"
  docker push "${ECR_REGISTRY_ID}.${ECR_REGION}.amazonaws.com/${ECR_REPOSITORY_NAME}:${ECR_IMAGE_TAG}"
}

# Validate required environment variables
validate_env_var "COUCHBASE_CONNECTION" "${COUCHBASE_CONNECTION}"
validate_env_var "COUCHBASE_USERNAME" "${COUCHBASE_USERNAME}"
validate_env_var "COUCHBASE_PASSWORD" "${COUCHBASE_PASSWORD}"
validate_env_var "MONGO_URI" "${MONGO_URI}"

# Validate URL formats
validate_url_format "${COUCHBASE_CONNECTION}"
validate_url_format "${MONGO_URI}"

# Main script
create_ecr_repository
login_to_ecr
build_and_push_image

# Create stack
aws cloudformation create-stack \
  --stack-name "camila-product-stack" \
  --template-body "file://camila-services-stack.yml" \
  --capabilities CAPABILITY_NAMED_IAM \
  --parameters \
    "ParameterKey=CouchbaseConnection,ParameterValue=${COUCHBASE_CONNECTION}" \
    "ParameterKey=CouchbaseUsername,ParameterValue=${COUCHBASE_USERNAME}" \
    "ParameterKey=CouchbasePassword,ParameterValue=${COUCHBASE_PASSWORD}" \
    "ParameterKey=MongoUri,ParameterValue=${MONGO_URI}"
