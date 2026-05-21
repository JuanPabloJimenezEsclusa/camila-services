#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"
workspace="$(pwd)"

# K6 test configuration variables
THREADS="${THREADS:-200}"
RAMP_UP="${RAMP_UP:-20}"
LOOPS="${LOOPS:-10}"

# App under test configuration variables
BASE_URL_PROTOCOL="${BASE_URL_PROTOCOL:-"http"}" # or https
BASE_URL="${BASE_URL:-"localhost"}" # or domain name: tech.jpje.xyz
BASE_URL_PORT="${BASE_URL_PORT:-"8080"}" # or 443.
BASE_PATH="${BASE_PATH:-"product-dev"}" # or product

# Oauth2.0
OAUTH_URL_PROTOCOL="${OAUTH_URL_PROTOCOL:-"http"}"
OAUTH_URL="${OAUTH_URL:-"localhost"}"
OAUTH_URL_PORT="${OAUTH_URL_PORT:-"9191"}"
OAUTH_PATH="${OAUTH_PATH:-"/realms/camila-realm/protocol/openid-connect/token"}"
OAUTH_GRANT_TYPE="${OAUTH_GRANT_TYPE:-"client_credentials"}"
OAUTH_SCOPE="${OAUTH_SCOPE:-"camila/read camila/write"}"
OAUTH_CLIENT_ID="${OAUTH_CLIENT_ID:-"camila-client"}"
OAUTH_CLIENT_SECRET="${OAUTH_CLIENT_SECRET:-"Fuvf8XyBDXxU57NAOOFZVvdUIPmGgiyE"}"

__prepareResultDirectory() {
  echo -e "${SEPARATOR} 📁 Preparing Result Directory ${SEPARATOR}"
  if [ -d "${workspace}/result" ]; then
    echo "✓ Result directory already exists at: ${workspace}/result"
    return 0
  fi
  echo "Creating result directory at: ${workspace}/result"
  sudo mkdir -p result && sudo chmod -R 777 result
}

__testExecution() {
  # https://grafana.com/docs/k6/latest/set-up/install-k6/?src=k6io&pg=oss-k6&plcmt=deploy-box-1#docker
  echo -e "${SEPARATOR} 🚀 K6 Test Execution ${SEPARATOR}"
  docker run --rm \
    --name k6-load-test \
    --env K6_WEB_DASHBOARD=true \
    --env K6_WEB_DASHBOARD_HOST=localhost \
    --env K6_WEB_DASHBOARD_PORT=5665 \
    --env K6_WEB_DASHBOARD_PERIOD=5s \
    --env BASE_URL_PROTOCOL="${BASE_URL_PROTOCOL}" \
    --env BASE_URL="${BASE_URL}" \
    --env BASE_URL_PORT="${BASE_URL_PORT}" \
    --env BASE_PATH="${BASE_PATH}" \
    --env OAUTH_URL_PROTOCOL="${OAUTH_URL_PROTOCOL}" \
    --env OAUTH_URL="${OAUTH_URL}" \
    --env OAUTH_URL_PORT="${OAUTH_URL_PORT}" \
    --env OAUTH_PATH="${OAUTH_PATH}" \
    --env OAUTH_GRANT_TYPE="${OAUTH_GRANT_TYPE}" \
    --env OAUTH_SCOPE="${OAUTH_SCOPE}" \
    --env OAUTH_CLIENT_ID="${OAUTH_CLIENT_ID}" \
    --env OAUTH_CLIENT_SECRET="${OAUTH_CLIENT_SECRET}" \
    --env THREADS="${THREADS}" \
    --env RAMP_UP="${RAMP_UP}" \
    --env LOOPS="${LOOPS}" \
    --volume "${workspace}/camila-product-api-load-tests.js":/camila-product-api-load-tests.js:rw \
    --volume "${workspace}/result":/result:rw \
    --network host \
    -i grafana/k6:master-with-browser run /camila-product-api-load-tests.js --console-output=/result/k6.log
}

main() {
  echo -e "${SEPARATOR} 🧪 K6 Load Test Configuration ${SEPARATOR}"
  echo "THREADS: ${THREADS} | RAMP_UP: ${RAMP_UP} | LOOPS: ${LOOPS}"
  echo "BASE_URL: ${BASE_URL_PROTOCOL}://${BASE_URL}:${BASE_URL_PORT}/${BASE_PATH}"
  echo "OAUTH_URL: ${OAUTH_URL_PROTOCOL}://${OAUTH_URL}:${OAUTH_URL_PORT}${OAUTH_PATH}"

  __prepareResultDirectory
  __testExecution
}

echo -e "${SEPARATOR} 🔨 Main: ${0} ${SEPARATOR}"
time main "$@"
