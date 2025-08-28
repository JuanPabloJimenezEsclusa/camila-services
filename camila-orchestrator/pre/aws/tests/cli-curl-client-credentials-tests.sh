#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

# Parameters
CLIENT_ID="1dmad5iclmo5ukvaoidmou6tl"
CLIENT_SECRET="3rk0gl5fi434m1tjrsu2uf97q8fn7riu1ia77mbc07egd8v76me"
SCOPES="camila/read camila/write"
TOKEN_ENDPOINT="https://camila-realm.auth.eu-west-1.amazoncognito.com/oauth2/token"

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

# Tests API with token
echo -e "\n ################################################# \n"
curl -Lvs -X GET 'https://tech.jpje.xyz/product/api/products/99999' \
  -H 'Accept: application/x-ndjson' \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" | \
   jq -r '[.id,.internalId,.name,.category,.salesUnits,(.stock|[.[]|tostring]|join(":")),.profitMargin,.daysInStock]|@csv' \
  | column -t -N 'ID,INTERNAL,NAME,CATEGORY,SALES,STOCK,PROFIT,DAYS' -s ',' -o '|'

echo -e "\n ################################################# \n"
curl -Lvs -X GET 'https://tech.jpje.xyz/product/api/products?profitMargin=0.01&stock=0.99&page=0&size=10000' \
  -H 'Accept: application/x-ndjson' \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" | \
   jq -r '[.id,.internalId,.name,.category,.salesUnits,(.stock|[.[]|tostring]|join(":")),.profitMargin,.daysInStock]|@csv' \
  | column -t -N 'ID,INTERNAL,NAME,CATEGORY,SALES,STOCK,PROFIT,DAYS' -s ',' -o '|'
