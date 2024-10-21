#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

cd "$(dirname "$0")/.."

# copiar scripts en el contenedor
docker cp .operate/data/ mongodb7:/

# ejecutar script base
docker exec -it mongodb7 \
  bash -c "mongosh -u camila -p camila < /data/minimum_data.script"

# importar N registros
# para regenerar 'data-generated.script' utilizar: 'RandomDataGenerator.java'
docker exec -it mongodb7 \
  bash -c "mongoimport \
  --uri mongodb://camilauser:camilauser@localhost:27017/camila-db \
  --type json \
  --collection products \
  --file /data/data-generated.script"
