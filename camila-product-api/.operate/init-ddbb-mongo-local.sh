#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

mongoUser="${1:-camila}"
mongoPassword="${2:-camila}"
mongoImportUser="${3:-camilauser}"
mongoImportPassword="${4:-camilauser}"
mongoShellScript="mongosh -u ${mongoUser} -p ${mongoPassword} < /data/mongo/minimum_data.script"
mongoImportScript="mongoimport \
  --uri mongodb://${mongoImportUser}:${mongoImportPassword}@localhost:27017/camila-db \
  --type json \
  --collection products \
  --file /data/mongo/data-generated.script"

cd "$(dirname "$0")/.."

# copiar scripts en el contenedor
docker cp .operate/data/ mongodb:/

# ejecutar script base
docker exec -it mongodb bash -c "${mongoShellScript}"

# importar N registros
# para regenerar 'data-generated.script' utilizar: 'RandomDataGenerator.java'
docker exec -it mongodb bash -c "${mongoImportScript}"
