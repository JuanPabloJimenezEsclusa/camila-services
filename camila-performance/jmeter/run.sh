#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

JMETER_TEST_PATH="${JMETER_TEST_PATH:-"."}" # This variable defines the path to the JMeter test plan configuration
THREADS="${THREADS:-200}" # This variable sets the number of concurrent users (threads) to simulate during the test
RAMP_UP="${RAMP_UP:-20}" # This variable specifies the duration (in seconds) for gradually increasing the load from 0 to the specified number of users
LOOPS="${LOOPS:-10}" # This variable defines the total number of times to iterate through the test

BASE_URL_PROTOCOL="${BASE_URL_PROTOCOL:-"https"}"
BASE_URL="${BASE_URL:-"tech.jpje.xyz"}"
BASE_URL_PORT="${BASE_URL_PORT:-"443"}"
BASE_PATH="${BASE_PATH:-"product"}"

# Oauth2.0
OAUTH_URL_PROTOCOL="${OAUTH_URL_PROTOCOL:-"http"}"
OAUTH_URL="${OAUTH_URL:-"localhost"}"
OAUTH_URL_PORT="${OAUTH_URL_PORT:-"9191"}"
OAUTH_PATH="${OAUTH_PATH:-"/realms/camila-realm/protocol/openid-connect/token"}"
OAUTH_GRANT_TYPE="${OAUTH_GRANT_TYPE:-"client_credentials"}"
OAUTH_SCOPE="${OAUTH_SCOPE:-"camila/read camila/write"}"
OAUTH_CLIENT_ID="${OAUTH_CLIENT_ID:-"camila-client"}"
OAUTH_CLIENT_SECRET="${OAUTH_CLIENT_SECRET:-"Fuvf8XyBDXxU57NAOOFZVvdUIPmGgiyE"}"

echo "JMETER_TEST_PATH: ${JMETER_TEST_PATH} | THREADS: ${THREADS} | RAMP_UP: ${RAMP_UP} | LOOPS: ${LOOPS}"
echo "BASE_URL: ${BASE_URL_PROTOCOL}://${BASE_URL}:${BASE_URL_PORT}/${BASE_PATH}"
echo "OAUTH_URL: ${OAUTH_URL_PROTOCOL}://${OAUTH_URL}:${OAUTH_URL_PORT}${OAUTH_PATH}"

# Test Workspace Cleanup
rm -dr "${JMETER_TEST_PATH}/reports" || true
rm -dr "${JMETER_TEST_PATH}/jmeter.log" || true

# JMeter Test Execution
time jmeter -n \
  -t "${JMETER_TEST_PATH}/camila-product-api.jmx" \
  -JTHREADS="${THREADS}" \
  -JRAMP_UP="${RAMP_UP}" \
  -JLOOPS="${LOOPS}" \
  -JBASE_URL_PROTOCOL="${BASE_URL_PROTOCOL}" \
  -JBASE_URL="${BASE_URL}" \
  -JBASE_URL_PORT="${BASE_URL_PORT}" \
  -JBASE_PATH="${BASE_PATH}" \
  -JOAUTH_URL_PROTOCOL="${OAUTH_URL_PROTOCOL}" \
  -JOAUTH_URL="${OAUTH_URL}" \
  -JOAUTH_URL_PORT="${OAUTH_URL_PORT}" \
  -JOAUTH_PATH="${OAUTH_PATH}" \
  -JOAUTH_GRANT_TYPE="${OAUTH_GRANT_TYPE}" \
  -JOAUTH_SCOPE="${OAUTH_SCOPE}" \
  -JOAUTH_CLIENT_ID="${OAUTH_CLIENT_ID}" \
  -JOAUTH_CLIENT_SECRET="${OAUTH_CLIENT_SECRET}" \
  -l "${JMETER_TEST_PATH}/reports/result.csv"

# JMeter HTML Report Generation
time jmeter \
  -g "${JMETER_TEST_PATH}/reports/result.csv" \
  -o "${JMETER_TEST_PATH}/reports/html"
