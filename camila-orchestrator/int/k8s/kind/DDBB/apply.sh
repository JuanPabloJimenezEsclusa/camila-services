#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

SEPARATOR="\n ################################################## \n"
attempt=0
max_attempts=12
success=false

cd "$(dirname "$0")"

echo -e "${SEPARATOR} 📦 Apply the k8s resources. ${SEPARATOR}"
kubectl apply -f ./camila-product-ddbb.yml

echo -e "${SEPARATOR} 🔑 Assign permissions to the hostpath-provisioner. ${SEPARATOR}"
while [ $attempt -lt $max_attempts ] && [ "${success}" = false ]; do
  attempt=$((attempt + 1))
  if sudo chmod a+w /tmp/hostpath-provisioner/data/mongo/ -R 2>/dev/null; then
    success=true
    echo "✅ Successfully assigned permissions on attempt ${attempt}."
  else
    echo "⚠️ Chmod attempt ${attempt} failed. Retrying in 10 seconds..."
    sleep 10
  fi
done

if [ "${success}" = false ]; then
  echo "Error: Failed to assign permissions after 2 minutes of attempts."
  exit 1;
fi

echo -e "${SEPARATOR} ⏳ Wait for the pod to be ready. ${SEPARATOR}"
kubectl wait --for=condition=ready pod -l app=mongo -n mongodb --timeout=5m

echo -e "${SEPARATOR} 📋 ${SEPARATOR}"
kubectl get all,sa,pv,pvc,resourcequotas,ingress -n mongodb -o wide --show-labels
