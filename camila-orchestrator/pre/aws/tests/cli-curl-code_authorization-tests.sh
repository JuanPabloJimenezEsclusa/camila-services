#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

# Manual step to get the CODE from the URL
# https://camila-realm.auth.eu-west-1.amazoncognito.com/login?response_type=code&client_id=j76r1no47rae60fhmaohm3d2s&redirect_uri=https://oauth.pstmn.io/v1/callback&scope=openid+camila/read+camila/write

# Parameters
CLIENT_ID="j76r1no47rae60fhmaohm3d2s"
CLIENT_SECRET="1pu4htts43vrchq7qdclmu8p4751vhtgb8rn9m186pvqo53oactk"
CODE="63d26b36-85a4-4e34-aec0-3f89171e1e96" # only works one time
REDIRECT_URI="https://oauth.pstmn.io/v1/callback"
TOKEN_ENDPOINT="https://camila-realm.auth.eu-west-1.amazoncognito.com/oauth2/token"

# Get tokens
response="$(curl -Ls -X POST "${TOKEN_ENDPOINT}" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}&code=${CODE}&redirect_uri=${REDIRECT_URI}")"
error="$(echo "${response}" | jq -r .error)"

# Check if error
if [ "${error}" == "invalid_grant" ]; then
  echo "Error: Unable to obtain tokens: ${response}"
  exit 1
fi

ACCESS_TOKEN=$(echo "${response}" | jq -r '.access_token')
ID_TOKEN=$(echo "${response}" | jq -r '.id_token')
REFRESH_TOKEN=$(echo "${response}" | jq -r '.refresh_token')

# Print tokens
echo -e "\n################# Access Token: #################\n$ACCESS_TOKEN\n"
echo -e "\n################# Id Token: #################\n$ID_TOKEN\n"
echo -e "\n################# Refresh Token: #################\n$REFRESH_TOKEN\n"

# Test API with token
curl -Lvs -X GET 'https://poc.jpje-kops.xyz/product/api/products?salesUnits=0.999&stock=0.001&page=0&size=20' \
  -H 'Accept: application/x-ndjson' \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" | jq -r '[.id,.internalId,.name,.category,.salesUnits,(.stock|[.[]|tostring]|join(":"))]|@csv' \
  | column -t -N 'ID,INTERNAL,NAME,CATEGORY,SALES,STOCK' -s ',' -o '|'
