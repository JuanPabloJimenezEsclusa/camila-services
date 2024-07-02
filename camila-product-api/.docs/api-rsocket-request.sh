#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o pipefail # Catch the error in do, function, script code.

cd "$(dirname "$0")"

# Require install: https://github.com/making/rsc
# Be careful, this is a not maintained project

# "wss://poc.jpje-kops.xyz:7001/product/api/rsocket"
# "ws://localhost:7000/product-dev/api/rsocket"

RSOCKET_SERVER_URL="${RSOCKET_SERVER_URL:-"wss://poc.jpje-kops.xyz:7001/product/api/rsocket"}"
RSOCKET_ENDPOINT="products.request-response-findByInternalId"
REQUEST_DATA='{ "internalId": "63132" }'
RESPONSE_FILE=$(mktemp)
echo -e "######################## (findByInternalId)\n01-RESPONSE_FILE: ${RESPONSE_FILE}\n######################## (findByInternalId)\n"

rsc --request \
  --route "${RSOCKET_ENDPOINT}" \
  --data "${REQUEST_DATA}" \
  --debug \
  "${RSOCKET_SERVER_URL}" > "${RESPONSE_FILE}"

RESPONSE="$(cat "${RESPONSE_FILE}")"
echo -e "######################## (findByInternalId)\n02-RESPONSE: ${RESPONSE}\n######################## (findByInternalId)\n"
rm "${RESPONSE_FILE}"

###################

RSOCKET_ENDPOINT="products.request-stream-sortByMetricsWeights"
REQUEST_DATA='{ "salesUnits": "0.001", "stock": "0.999", "page": "0", "size": "10" }'
RESPONSE_FILE=$(mktemp)
echo -e "######################## (sortByMetricsWeights)\n01-RESPONSE_FILE: ${RESPONSE_FILE}\n######################## (sortByMetricsWeights)\n"

rsc --stream \
  --route "${RSOCKET_ENDPOINT}" \
  --data "${REQUEST_DATA}" \
  --take 5 \
  --debug \
  "${RSOCKET_SERVER_URL}" > "${RESPONSE_FILE}"

RESPONSE="$(cat "${RESPONSE_FILE}")"
echo -e "######################## (sortByMetricsWeights)\n02-RESPONSE: ${RESPONSE}\n######################## (sortByMetricsWeights)\n"
rm "${RESPONSE_FILE}"
