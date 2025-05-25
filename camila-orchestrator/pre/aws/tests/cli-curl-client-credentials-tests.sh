#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

# Parameters
CLIENT_ID="63po2nk910cprelfcvpvbv7ekk"
CLIENT_SECRET="siql4p03g183lu2bj29ftdf11faif6v09pcelb34sk681cklgar"
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
curl -Lvs -X GET 'https://poc.jpje-kops.xyz/product/api/products/99999' \
  -H 'Accept: application/x-ndjson' \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" | \
   jq -r '[.id,.internalId,.name,.category,.salesUnits,(.stock|[.[]|tostring]|join(":")),.profitMargin,.daysInStock]|@csv' \
  | column -t -N 'ID,INTERNAL,NAME,CATEGORY,SALES,STOCK,PROFIT,DAYS' -s ',' -o '|'

echo -e "\n ################################################# \n"
curl -Lvs -X GET 'https://poc.jpje-kops.xyz/product/api/products?profitMargin=0.50&stock=0.50&page=0&size=1000' \
  -H 'Accept: application/x-ndjson' \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" | \
   jq -r '[.id,.internalId,.name,.category,.salesUnits,(.stock|[.[]|tostring]|join(":")),.profitMargin,.daysInStock]|@csv' \
  | column -t -N 'ID,INTERNAL,NAME,CATEGORY,SALES,STOCK,PROFIT,DAYS' -s ',' -o '|'
