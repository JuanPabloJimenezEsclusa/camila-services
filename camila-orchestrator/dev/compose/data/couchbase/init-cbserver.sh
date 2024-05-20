#!/bin/bash
# used to start couchbase server - can't get around this as docker compose only allows you to start one command - so we have to start couchbase like the standard couchbase Dockerfile would
# https://github.com/couchbase/docker/blob/master/enterprise/couchbase-server/7.0.3/Dockerfile#L82
# https://github.com/couchbaselabs/couchbase-docker-compose.git

/entrypoint.sh couchbase-server &

# track if setup is complete so we don't try to setup again
FILE='/opt/couchbase/init/setupComplete.txt'

if ! [ -f "$FILE" ]; then
  # used to automatically create the cluster based on environment variables
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-cluster-init.html

  echo "${COUCHBASE_ADMINISTRATOR_USERNAME}:${COUCHBASE_ADMINISTRATOR_PASSWORD}"
  sleep 10s

  /opt/couchbase/bin/couchbase-cli cluster-init \
  -c localhost \
  --cluster-username "${COUCHBASE_ADMINISTRATOR_USERNAME}" \
  --cluster-password "${COUCHBASE_ADMINISTRATOR_PASSWORD}"\
  --services data,index,query \
  --cluster-ramsize $COUCHBASE_RAM_SIZE \
  --cluster-index-ramsize $COUCHBASE_INDEX_RAM_SIZE \
  --index-storage-setting default

  echo "step 2"
  sleep 1s

  # used to auto create the bucket based on environment variables
  # https://docs.couchbase.com/server/current/cli/cbcli/couchbase-cli-bucket-create.html

  /opt/couchbase/bin/couchbase-cli bucket-create \
  --cluster localhost:8091 \
  --username "${COUCHBASE_ADMINISTRATOR_USERNAME}" \
  --password "${COUCHBASE_ADMINISTRATOR_PASSWORD}"\
  --bucket "${COUCHBASE_BUCKET}" \
  --bucket-ramsize "${COUCHBASE_BUCKET_RAMSIZE}" \
  --bucket-type couchbase

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

  echo "step 5"
  sleep 1s

  # create indexes using the QUERY REST API
  # https://docs.couchbase.com/server/current/n1ql/n1ql-rest-api/index.html

  /opt/couchbase/bin/curl -v http://localhost:8093/query/service \
  -u "${COUCHBASE_ADMINISTRATOR_USERNAME}:${COUCHBASE_ADMINISTRATOR_PASSWORD}"\
  -d 'statement=CREATE INDEX adv_internalId ON `default`:`camila-product-bucket`.`product`.`products`(`internalId`)'

  /opt/couchbase/bin/curl -v http://localhost:8093/query/service \
  -u "${COUCHBASE_ADMINISTRATOR_USERNAME}:${COUCHBASE_ADMINISTRATOR_PASSWORD}"\
  -d 'statement=CREATE INDEX adv_internalId_name_stock_category_salesUnits ON `default`:`camila-product-bucket`.`product`.`products`(`internalId`,`name`,`category`,`salesUnits`,`stock`)'

  echo "step 6"
  sleep 1s

  # import sample data into the bucket
  # https://docs.couchbase.com/server/current/tools/cbimport-json.html

  /opt/couchbase/bin/cbimport json --format lines \
  -c localhost:8091 \
  -u "${COUCHBASE_ADMINISTRATOR_USERNAME}" \
  -p "${COUCHBASE_ADMINISTRATOR_PASSWORD}" \
  -d "file:///opt/couchbase/init/sample-data.script"  \
  -b "${COUCHBASE_BUCKET}" \
  --scope-collection-exp "${COUCHBASE_BUCKET_SCOPE}.${COUCHBASE_BUCKET_COLLECTION}" \
  -g "#UUID#"

  # create file so we know that the cluster is setup and don't run the setup again
  touch $FILE
fi

# docker compose will stop the container from running unless we do this
# known issue and workaround
tail -f /dev/null
