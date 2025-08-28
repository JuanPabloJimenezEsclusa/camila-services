#!/bin/bash

# EKS Access Configuration Helper Script
# This script helps fix the "Your current IAM principal doesn't have access to Kubernetes objects on this cluster" error

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

CLUSTER_NAME="camila-eks-cluster"
REGION="eu-west-1"
STACK_NAME="camila-eks-stack"

echo "=== EKS Access Configuration Helper ==="

__check_aws_cli() {
  if ! command -v aws &> /dev/null; then
    echo "❌ AWS CLI is not installed. Please install it first."
    exit 1
  fi
  echo "✅ AWS CLI is available"
}

# Function to check current identity
__check_current_identity() {
  echo "🔍 Checking current AWS identity..."
  CURRENT_IDENTITY=$(aws sts get-caller-identity)
  CURRENT_ARN=$(echo "${CURRENT_IDENTITY}" | jq -r '.Arn')
  CURRENT_USER=$(echo "${CURRENT_IDENTITY}" | jq -r '.UserId')
  ACCOUNT_ID=$(echo "${CURRENT_IDENTITY}" | jq -r '.Account')

  echo "Current IAM Principal: ${CURRENT_ARN}"
  echo "Current User: ${CURRENT_USER}"
  echo "Account ID: ${ACCOUNT_ID}"

  # Store for later use
  export CURRENT_ARN
  export ACCOUNT_ID
}

__update_stack_with_access() {
  echo "🚀 Updating CloudFormation stack to grant access to current user..."

  aws cloudformation update-stack \
    --stack-name "${STACK_NAME}" \
    --use-previous-template \
    --parameters \
        ParameterKey=AdminUserArn,ParameterValue="${CURRENT_ARN}" \
        ParameterKey=AdminRoleArn,ParameterValue="" \
    --capabilities CAPABILITY_NAMED_IAM \
    --region "${REGION}"

  echo "✅ Stack update initiated. Waiting for completion..."
  aws cloudformation wait stack-update-complete --stack-name "${STACK_NAME}" --region "${REGION}"
  echo "✅ Stack update completed!"
}

__update_kubeconfig() {
  echo "🔧 Updating kubeconfig..."
  aws eks update-kubeconfig --region "${REGION}" --name "${CLUSTER_NAME}"
  echo "✅ Kubeconfig updated"
}

__test_cluster_access() {
  echo "🧪 Testing cluster access..."
  if kubectl get nodes; then
    echo "✅ Successfully connected to EKS cluster!"
    echo "✅ You now have admin access to the cluster"
  else
    echo "❌ Still cannot access the cluster. Check the troubleshooting steps below."
    return 1
  fi
}

__add_additional_access() {
  echo ""
  echo "📋 To add additional users or roles to the cluster:"
  echo ""
  echo "1. Update the CloudFormation stack with additional parameters:"
  echo "   aws cloudformation update-stack \\"
  echo "     --stack-name ${STACK_NAME} \\"
  echo "     --use-previous-template \\"
  echo "     --parameters \\"
  echo "       ParameterKey=AdminUserArn,ParameterValue=\"${CURRENT_ARN}\" \\"
  echo "       ParameterKey=AdminRoleArn,ParameterValue=\"arn:aws:iam::${ACCOUNT_ID}:role/YourRoleName\" \\"
  echo "       ParameterKey=DeveloperUserArns,ParameterValue=\"arn:aws:iam::${ACCOUNT_ID}:user/dev1,arn:aws:iam::${ACCOUNT_ID}:user/dev2\" \\"
  echo "     --capabilities CAPABILITY_NAMED_IAM \\"
  echo "     --region ${REGION}"
  echo ""
  echo "2. Or use AWS CLI to add access entries directly:"
  echo "   aws eks create-access-entry \\"
  echo "     --cluster-name ${CLUSTER_NAME} \\"
  echo "     --principal-arn arn:aws:iam::${ACCOUNT_ID}:user/username \\"
  echo "     --type STANDARD \\"
  echo "     --region ${REGION}"
  echo ""
  echo "   aws eks associate-access-policy \\"
  echo "     --cluster-name ${CLUSTER_NAME} \\"
  echo "     --principal-arn arn:aws:iam::${ACCOUNT_ID}:user/username \\"
  echo "     --policy-arn arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy \\"
  echo "     --access-scope type=cluster \\"
  echo "     --region ${REGION}"
}

# Function to show troubleshooting steps
__show_troubleshooting() {
  echo ""
  echo "🔍 TROUBLESHOOTING STEPS:"
  echo ""
  echo "1. Verify your AWS credentials are configured correctly:"
  echo "   aws sts get-caller-identity"
  echo ""
  echo "2. Check if the cluster exists and is active:"
  echo "   aws eks describe-cluster --name ${CLUSTER_NAME} --region ${REGION}"
  echo ""
  echo "3. List current access entries:"
  echo "   aws eks list-access-entries --cluster-name ${CLUSTER_NAME} --region ${REGION}"
  echo ""
  echo "4. Check if kubeconfig is correct:"
  echo "   kubectl config current-context"
  echo "   kubectl config get-contexts"
  echo ""
  echo "5. Try updating kubeconfig again:"
  echo "   aws eks update-kubeconfig --region ${REGION} --name ${CLUSTER_NAME} --alias ${CLUSTER_NAME}"
  echo ""
  echo "6. If you're using an assumed role, make sure the role has EKS permissions:"
  echo "   - eks:DescribeCluster"
  echo "   - eks:ListClusters"
  echo ""
  echo "7. Check cluster authentication mode:"
  echo "   aws eks describe-cluster --name ${CLUSTER_NAME} --region ${REGION} --query 'cluster.accessConfig'"
}

# Main function
main() {
  __check_aws_cli
  __check_current_identity

  echo "Do you want to update the CloudFormation stack to grant access to your current IAM principal? (y/N)"
  read -r response
  if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
    __update_stack_with_access
    __update_kubeconfig
    sleep 10
    if __test_cluster_access; then
      echo "🎉 SUCCESS! Your EKS access issue has been resolved!"
      __add_additional_access
    else
      __show_troubleshooting
    fi
  else
    echo ""
    echo "Manual steps to fix the access issue:"
    echo ""
    echo "1. Update your CloudFormation stack with your user ARN:"
    echo "   AdminUserArn: ${CURRENT_ARN}"
    echo ""
    echo "2. Or manually create an access entry:"
    echo "   aws eks create-access-entry --cluster-name ${CLUSTER_NAME} --principal-arn \"${CURRENT_ARN}\" --type STANDARD --region ${REGION}"
    echo "   aws eks associate-access-policy --cluster-name ${CLUSTER_NAME} --principal-arn \"${CURRENT_ARN}\" --policy-arn arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy --access-scope type=cluster --region ${REGION}"
    echo ""
    echo "3. Update your kubeconfig:"
    echo "   aws eks update-kubeconfig --region ${REGION} --name ${CLUSTER_NAME}"
    echo ""
    __show_troubleshooting
  fi
}

# Check if script is being sourced or executed
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  main "$@"
fi
