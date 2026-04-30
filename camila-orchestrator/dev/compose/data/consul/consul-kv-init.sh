#!/usr/bin/env bash

# Initializes Consul KV store with per-service configuration.

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR

CONSUL_HTTP_ADDR="${CONSUL_HTTP_ADDR:-http://consul:8500}"

wait_for_consul() {
  echo "Waiting for Consul at ${CONSUL_HTTP_ADDR}..."
  until consul members -http-addr="${CONSUL_HTTP_ADDR}" > /dev/null 2>&1; do
    sleep 2
  done
  echo "Consul ready."
}

kv_put() {
  consul kv put -http-addr="${CONSUL_HTTP_ADDR}" "$1" - <<EOF
$2
EOF
}

wait_for_consul

kv_put "config/camila-admin,dev/data" \
'info:
  app:
    description: "CAMILA-ADMIN-DEV-20260430"
    author: "JPJE_DEV_1@test.com"
    environments:
      - "loc"
      - "dev"
      - "int"
'

kv_put "config/camila-gateway,dev/data" \
'info:
  app:
    description: "CAMILA-GATEWAY-DEV-20260430"
    author: "JPJE_DEV_1@test.com"
    environments:
      - "loc"
      - "dev"
      - "int"
      - "pre"
      - "pro"

gateway:
  replenishRate: 250
  burstCapacity: 500
  requestedTokens: 1
  fallback:
    message: >-
      Gateway is currently unable to handle the request due to a temporary overloading or maintenance of the server.
      Please try again later. If the problem persists, please contact the system administrator.
      The error code is: 502
'

kv_put "config/camila-product-api,dev/data" \
'info:
  app:
    description: "CAMILA-PRODUCT-API-DEV-20260430"
    author: "JPJE_DEV_1@test.com"
    environments:
      - "loc"
      - "dev"
      - "int"
'

echo "Consul KV init complete."
