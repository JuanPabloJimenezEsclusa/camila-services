#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

echo -e "${SEPARATOR} Apply the k8s resources. ${SEPARATOR}"
kubectl apply -n test -f ./smoke-test.yml

echo -e "${SEPARATOR} Wait for the pod to be ready. ${SEPARATOR}"
kubectl wait --for=condition=ready --timeout=5m pod/test-0 -n test

echo -e "${SEPARATOR} Get all the resources. ${SEPARATOR}"
kubectl get all,sa,pvc,pv -n test

echo -e "${SEPARATOR} Get the logs. ${SEPARATOR}"
kubectl logs -n test test-0

echo -e "${SEPARATOR} Port forward the stateful set. ${SEPARATOR}"
kubectl port-forward statefulset/test 8888:80 -n test
