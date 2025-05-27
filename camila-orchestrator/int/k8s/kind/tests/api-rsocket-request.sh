#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o pipefail # Catch the error in do, function, script code.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

# Require install: https://github.com/making/rsc
# Be careful, this is a not maintained project

RSOCKET_SERVER_URL="${RSOCKET_SERVER_URL:-"ws://localhost:7000/product-int/api/rsocket"}"
RSOCKET_ENDPOINT="products.request-response-findByInternalId"
REQUEST_DATA='{ "internalId": "1000" }'
RESPONSE_FILE=$(mktemp)
echo -e "${SEPARATOR} 🔌 REQUEST (findByInternalId) ${SEPARATOR}"
echo -e "📝 Response file: ${RESPONSE_FILE}"
echo -e "🎯 Endpoint: ${RSOCKET_ENDPOINT}"
echo -e "📤 Request data: ${REQUEST_DATA}"

echo -e "🚀 Sending request..."
rsc --request \
  --route "${RSOCKET_ENDPOINT}" \
  --data "${REQUEST_DATA}" \
  --debug \
  --stacktrace \
  "${RSOCKET_SERVER_URL}" > "${RESPONSE_FILE}" || true

RESPONSE="$(cat "${RESPONSE_FILE}")"
echo -e "${SEPARATOR} 📊 RESPONSE (findByInternalId) ${SEPARATOR}"
echo -e "${RESPONSE}"
echo -e "🧹 Cleaning up temporary file..."
rm "${RESPONSE_FILE}"

###################

RSOCKET_ENDPOINT="products.request-stream-sortByMetricsWeights"
REQUEST_DATA='{ "salesUnits": "0.001", "stock": "0.999", "page": "0", "size": "10" }'
RESPONSE_FILE=$(mktemp)
echo -e "${SEPARATOR} 🔌 REQUEST (sortByMetricsWeights) ${SEPARATOR}"
echo -e "📝 Response file: ${RESPONSE_FILE}"
echo -e "🎯 Endpoint: ${RSOCKET_ENDPOINT}"
echo -e "📤 Request data: ${REQUEST_DATA}"

echo -e "🚀 Sending request..."
rsc --stream \
  --route "${RSOCKET_ENDPOINT}" \
  --data "${REQUEST_DATA}" \
  --take 5 \
  --debug \
  --stacktrace \
  "${RSOCKET_SERVER_URL}" > "${RESPONSE_FILE}" || true

RESPONSE="$(cat "${RESPONSE_FILE}")"
echo -e "${SEPARATOR} 📊 RESPONSE (sortByMetricsWeights) ${SEPARATOR}"
echo -e "${RESPONSE}"
echo -e "🧹 Cleaning up temporary file..."
rm "${RESPONSE_FILE}"
