#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

echo -e "${SEPARATOR} 🗑️ Delete the namespace. ${SEPARATOR}"
kubectl delete namespaces camila-product-api-serveless-ns --grace-period=0 --force

echo -e "${SEPARATOR} 🗑️ Remove image. ${SEPARATOR}"
docker rmi \
  docker.io/library/camila-product-api-serveless:1.0.0 \
  kind-registry:5000/camila-product-api-serveless:1.0.0 \
  172.28.0.6:5000/camila-product-api-serveless:1.0.0 --force || true

echo -e "${SEPARATOR} 🗑️ Clean local maven repository. ${SEPARATOR}"
rm -rdf ~/.m2/repository/com/camila/ || true
