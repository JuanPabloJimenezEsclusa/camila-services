#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o pipefail # Catch the error in do, function, script code.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

# Require install: https://github.com/making/rsc
# Be careful, this is a not maintained project

CLIENT_ID="2ijh0c0eo1fmgrip5jaqm96l19"
CLIENT_SECRET="15sgk84o0qtt839jv01qeo0jqpfsk8f3cj8ri0mb841qbu4ut61j"
SCOPES="camila/read camila/write"
TOKEN_ENDPOINT="https://camila-realm.auth.eu-west-1.amazoncognito.com/oauth2/token"
RSOCKET_SERVER_URL="${RSOCKET_SERVER_URL:-"wss://tech.jpje.xyz:443/product/api/rsocket"}"
RSOCKET_ENDPOINT="products.request-response-findByInternalId"
REQUEST_DATA='{ "internalId": "1000" }'
RESPONSE_FILE=$(mktemp)

# Get tokens
response="$(curl -Ls -X POST "${TOKEN_ENDPOINT}" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}&scopes=${SCOPES}")"
error="$(echo "${response}" | jq -r .error)"

# Check if error
if [ "${error}" == "invalid_grant" ]; then
  echo "Error: Unable to obtain tokens: ${response}"
  exit 1
fi

# Get access token
ACCESS_TOKEN=$(echo "${response}" | jq -r '.access_token')
echo -e "\n ################# Access Token: ################# \n$ACCESS_TOKEN\n"

echo -e "${SEPARATOR} 🔌 REQUEST (findByInternalId) ${SEPARATOR}"
echo -e "📝 Response file: ${RESPONSE_FILE}"
echo -e "🎯 Endpoint: ${RSOCKET_ENDPOINT}"
echo -e "📤 Request data: ${REQUEST_DATA}"

echo -e "🚀 Sending request..."
rsc --request \
  --route "${RSOCKET_ENDPOINT}" \
  --data "${REQUEST_DATA}" \
  --wsHeader "Authorization: Bearer ${ACCESS_TOKEN}" \
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
REQUEST_DATA='{ "salesUnits": "0.001", "stock": "0.997", "profitMargin": "0.001", "daysInStock": "0.001", "page": "0", "size": "10" }'
RESPONSE_FILE=$(mktemp)
echo -e "${SEPARATOR} 🔌 REQUEST (sortByMetricsWeights) ${SEPARATOR}"
echo -e "📝 Response file: ${RESPONSE_FILE}"
echo -e "🎯 Endpoint: ${RSOCKET_ENDPOINT}"
echo -e "📤 Request data: ${REQUEST_DATA}"

echo -e "🚀 Sending request..."
rsc --stream \
  --route "${RSOCKET_ENDPOINT}" \
  --data "${REQUEST_DATA}" \
  --wsHeader "Authorization: Bearer ${ACCESS_TOKEN}" \
  --take 5 \
  --debug \
  --stacktrace \
  "${RSOCKET_SERVER_URL}" > "${RESPONSE_FILE}" || true

RESPONSE="$(cat "${RESPONSE_FILE}")"
echo -e "${SEPARATOR} 📊 RESPONSE (sortByMetricsWeights) ${SEPARATOR}"
echo -e "${RESPONSE}"
echo -e "🧹 Cleaning up temporary file..."
rm "${RESPONSE_FILE}"
