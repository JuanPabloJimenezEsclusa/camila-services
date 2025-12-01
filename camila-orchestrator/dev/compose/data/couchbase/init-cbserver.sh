#!/usr/bin/env bash

# used to start couchbase server - can't get around this as docker compose only allows you to start one command - so we have to start couchbase like the standard couchbase Dockerfile would
# https://github.com/couchbase/docker/blob/master/enterprise/couchbase-server/7.0.3/Dockerfile#L82
# https://github.com/couchbaselabs/couchbase-docker-compose.git

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.


# Fix locale warning by setting a safe default locale
export LC_ALL=C.UTF-8
export LANG=C.UTF-8

COUCHBASE_ADMINISTRATOR_USERNAME="${COUCHBASE_ADMINISTRATOR_USERNAME:-admin}"
COUCHBASE_ADMINISTRATOR_PASSWORD="${COUCHBASE_ADMINISTRATOR_PASSWORD:-password}"
COUCHBASE_RAM_SIZE="${COUCHBASE_RAM_SIZE:-512}"
COUCHBASE_INDEX_RAM_SIZE="${COUCHBASE_INDEX_RAM_SIZE:-256}"
COUCHBASE_BUCKET="${COUCHBASE_BUCKET:-camila-product-bucket}"
COUCHBASE_BUCKET_RAMSIZE="${COUCHBASE_BUCKET_RAMSIZE:-256}"
COUCHBASE_BUCKET_SCOPE="${COUCHBASE_BUCKET_SCOPE:-product}"
COUCHBASE_BUCKET_COLLECTION="${COUCHBASE_BUCKET_COLLECTION:-products}"

/entrypoint.sh couchbase-server &

# Function to wait for Couchbase to be ready
__wait_for_couchbase() {
  echo "Waiting for Couchbase to start..."
  for i in {1..30}; do
    if curl -s http://localhost:8091/pools > /dev/null; then
      echo "Couchbase is running!"
      return 0
    fi
    echo "Waiting for Couchbase to start (attempt $i/30)..."
    sleep 5
  done
  echo "ERROR: Couchbase failed to start within 150 seconds"
  return 1
}

main() {
  echo "Starting Couchbase initialization..."
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-cluster-init.html
  echo "${COUCHBASE_ADMINISTRATOR_USERNAME}:${COUCHBASE_ADMINISTRATOR_PASSWORD}"

  # Wait for Couchbase to be ready
  if ! __wait_for_couchbase; then
    exit 1
  fi

  /opt/couchbase/bin/couchbase-cli cluster-init \
  --cluster localhost:8091 \
  --cluster-username "${COUCHBASE_ADMINISTRATOR_USERNAME}" \
  --cluster-password "${COUCHBASE_ADMINISTRATOR_PASSWORD}" \
  --cluster-ramsize "${COUCHBASE_RAM_SIZE}" \
  --cluster-index-ramsize "${COUCHBASE_INDEX_RAM_SIZE}" \
  --services data,index,query \
  --index-storage-setting default

  if [ $? -ne 0 ]; then
    echo "Error: Cluster initialization failed"
    exit 1
  fi

  echo "step 2"
  sleep 1s

  # used to auto create the bucket based on environment variables
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-bucket-create.html

  /opt/couchbase/bin/couchbase-cli bucket-create \
  --cluster localhost:8091 \
  --username "${COUCHBASE_ADMINISTRATOR_USERNAME}" \
  --password "${COUCHBASE_ADMINISTRATOR_PASSWORD}" \
  --bucket "${COUCHBASE_BUCKET}" \
  --bucket-ramsize "${COUCHBASE_BUCKET_RAMSIZE}" \
  --bucket-type couchbase

  if [ $? -ne 0 ]; then
    echo "Error: Bucket creation failed"
    exit 1
  fi

  echo "step 3"
  sleep 1s

  # used to auto create the scope based on environment variables
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-collection-manage.html

  /opt/couchbase/bin/couchbase-cli collection-manage \
  --cluster localhost:8091 \
  --username "${COUCHBASE_ADMINISTRATOR_USERNAME}" \
  --password "${COUCHBASE_ADMINISTRATOR_PASSWORD}"\
  --bucket "${COUCHBASE_BUCKET}" \
  --create-scope "${COUCHBASE_BUCKET_SCOPE}"

  if [ $? -ne 0 ]; then
    echo "Error: Scope creation failed"
    exit 1
  fi

  echo "step 4"
  sleep 1s

  # used to auto create the collection based on environment variables
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-collection-manage.html

  /opt/couchbase/bin/couchbase-cli collection-manage \
  --cluster localhost:8091 \
  --username "${COUCHBASE_ADMINISTRATOR_USERNAME}" \
  --password "${COUCHBASE_ADMINISTRATOR_PASSWORD}"\
  --bucket "${COUCHBASE_BUCKET}" \
  --create-collection "${COUCHBASE_BUCKET_SCOPE}.${COUCHBASE_BUCKET_COLLECTION}"

  if [ $? -ne 0 ]; then
    echo "Error: Collection creation failed"
    exit 1
  fi

  echo "step 5"
  sleep 5s

  # import sample data into the bucket
  # https://docs.couchbase.com/server/current/tools/cbimport-json.html

  /opt/couchbase/bin/cbimport json --format lines \
  -c localhost:8091 \
  -u "${COUCHBASE_ADMINISTRATOR_USERNAME}" \
  -p "${COUCHBASE_ADMINISTRATOR_PASSWORD}" \
  -d "file:///opt/couchbase/init/sample-data.jsonl"  \
  -b "${COUCHBASE_BUCKET}" \
  --scope-collection-exp "${COUCHBASE_BUCKET_SCOPE}.${COUCHBASE_BUCKET_COLLECTION}" \
  -g "#UUID#"

  if [ $? -ne 0 ]; then
    echo "Error: Sample data import failed"
    exit 1
  fi

  echo "step 6"
  sleep 15s

  # create indexes using the QUERY REST API
  # https://docs.couchbase.com/server/current/n1ql/n1ql-rest-api/index.html
  /opt/couchbase/bin/curl -v http://localhost:8093/query/service \
  -u "${COUCHBASE_ADMINISTRATOR_USERNAME}:${COUCHBASE_ADMINISTRATOR_PASSWORD}"\
  -d 'statement=CREATE INDEX adv_internalId ON `default`:`camila-product-bucket`.`product`.`products`(`internalId`)'

  /opt/couchbase/bin/curl -v http://localhost:8093/query/service \
  -u "${COUCHBASE_ADMINISTRATOR_USERNAME}:${COUCHBASE_ADMINISTRATOR_PASSWORD}"\
  -d 'statement=CREATE INDEX adv_internalId_name_stock_category_salesUnits ON `default`:`camila-product-bucket`.`product`.`products`(`internalId`,`name`,`category`,`salesUnits`,`stock`,`profitMargin`,`daysInStock`)'

  echo "All initialization steps completed successfully."
}

# Run the main function
main

# docker compose will stop the container from running unless we do this
# known issue and workaround
tail -f /dev/null
