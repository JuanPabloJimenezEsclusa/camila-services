#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

echo -e "${SEPARATOR}✏️ Update /etc/hosts file. ${SEPARATOR}"
if grep -q "kind-registry" /etc/hosts; then
  echo "🔄 Updating existing entry..."
  sudo sed -i "s/.*kind-registry$/172.18.0.6 kind-registry/" /etc/hosts
else
  echo "➕ Adding new entry..."
  echo "172.18.0.6 kind-registry" | sudo tee -a /etc/hosts > /dev/null
fi

echo -e "${SEPARATOR}📦 Install clients and start cluster. ${SEPARATOR}"
cd clusters
./install.sh
./start-cluster.sh

echo -e "${SEPARATOR}🚀 Install DDBB. ${SEPARATOR}"
cd ../DDBB
./apply.sh

echo -e "${SEPARATOR}🚀 Fill DDBB with minimum data. ${SEPARATOR}"
mongo_pod="$(kubectl get pods -n mongodb --selector=app=mongo -o jsonpath='{.items[*].metadata.name}')" && \
kubectl exec -it --namespace=mongodb "${mongo_pod}" -- \
  bash -c "mongosh --host localhost:27017 \
           --username adminuser \
           --password password123 \
           --authenticationDatabase admin" < ../../../../dev/compose/data/mongodb/minimum_data.script

echo -e "${SEPARATOR}🚀 Install API. ${SEPARATOR}"
cd ../API/serveless
./apply-serveless.sh
