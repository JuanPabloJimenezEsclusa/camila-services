#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR

# Parameters
CLIENT_ID="4cj70retrim9fjj4kfld99t9hf"
CLIENT_SECRET="1e4g22ue5f2dkmlv7r8hau7k5t5mbc3o0ut9m4b4tbb9vpdh8vn2"
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
echo -e "\n################# Access Token: #################\n$ACCESS_TOKEN\n"

# Tests API with token
curl -Lvs -X GET 'https://poc.jpje-kops.xyz/product/api/products/99999' \
  -H 'Accept: application/x-ndjson' \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" | jq .

curl -Lvs -X GET 'https://poc.jpje-kops.xyz/product/api/products?salesUnits=0.001&stock=0.999&page=0&size=500' \
  -H 'Accept: application/x-ndjson' \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" | jq .
