#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

cd "$(dirname "$0")"

kubectl delete namespaces test --force --grace-period=0
kubectl delete persistentvolume/pvc-e03ceee8-6c38-4288-97f1-9b682ba87d6e --force --grace-period=0
kubectl get all,pvc,pv -n test
