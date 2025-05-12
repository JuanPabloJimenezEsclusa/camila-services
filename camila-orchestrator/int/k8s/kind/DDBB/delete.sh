#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

echo -e "${SEPARATOR}🗑️ Delete the namespace. ${SEPARATOR}"
kubectl delete namespaces mongodb --grace-period=0 --force
kubectl delete persistentvolume/mongo-data-pv --grace-period=0 --force
