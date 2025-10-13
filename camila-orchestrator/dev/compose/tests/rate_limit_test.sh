#!/usr/bin/env bash

# Integration test script for gateway rate-limiter and fallback
# Usage:
#   ./rate_limit_test.sh [--url URL] [--burst N] [--concurrency C]
# Example:
#   ./rate_limit_test.sh --url http://gateway:8090/product-dev/api/products-dev/1 --burst 10000 --concurrency 20

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

URL="${1:-http://gateway:8090/product-dev/api/products-dev/1}"
BURST=50
CONCURRENCY=20
PROM_URL="${PROM_URL:-http://prometheus:9090}"
GATEWAY_PROM="${GATEWAY_PROM:-http://gateway:8090/actuator/prometheus}"

# Oauth2
CLIENT_ID="camila-client"
CLIENT_SECRET="Fuvf8XyBDXxU57NAOOFZVvdUIPmGgiyE"
SCOPES="camila/read camila/write"
TOKEN_ENDPOINT="http://keycloak:9191/realms/camila-realm/protocol/openid-connect/token"

# parse args (simple)
while [[ $# -gt 0 ]]; do
  case "${1}" in
    --url) URL="${2}"; shift 2;;
    --burst) BURST="${2}"; shift 2;;
    --concurrency) CONCURRENCY="${2}"; shift 2;;
    --prom) PROM_URL="${2}"; shift 2;;
    --gateway-prom) GATEWAY_PROM="${2}"; shift 2;;
    *) shift;;
  esac
done

echo "Test config: URL=${URL} BURST=${BURST} CONCURRENCY=${CONCURRENCY} PROM=${PROM_URL} GATEWAY_PROM=${GATEWAY_PROM}"

__get_token() {
  response="$(curl -Ls -X POST "${TOKEN_ENDPOINT}" \
    --header 'Content-Type: application/x-www-form-urlencoded' \
    --data-urlencode 'grant_type=client_credentials' \
    --data-urlencode "client_id=${CLIENT_ID}" \
    --data-urlencode "client_secret=${CLIENT_SECRET}" \
    --data-urlencode "scope=${SCOPES}")"
  error="$(echo "${response}" | jq -r .error)"

  # Check if error
  if [ "${error}" == "invalid_grant" ]; then
    echo "Error: Unable to obtain tokens: ${response}"
    exit 1
  fi

  # Get access token
  ACCESS_TOKEN=$(echo "${response}" | jq -r '.access_token')
  echo -e "\n ################# Access Token: ################# \n$ACCESS_TOKEN\n"
}

__fire_burst() {
  echo "Firing burst of ${BURST} requests with concurrency ${CONCURRENCY}..."
  seq 1 "$BURST" | xargs -n1 -P "${CONCURRENCY}" -I{} bash -c "
    curl -s -o /dev/null -w \"%{http_code}\" -H \"Authorization: Bearer ${ACCESS_TOKEN}\" -X GET \"${URL}\"
  "
  echo
}

__query_gateway_metrics() {
  echo "Gateway prometheus scrape (first 20 lines):"
  curl -sS "${GATEWAY_PROM}" | sed -n '1,20p'
}

__query_429_rate() {
  echo "429 count in last 1m (gateway metrics):"
  if command -v jq >/dev/null 2>&1; then
    curl -sG "${PROM_URL}/api/v1/query" --data-urlencode "query=sum(rate(http_server_requests_seconds_count{status=\"429\"}[1m]))" | jq -r '.data.result'
  else
    curl -sG "${PROM_URL}/api/v1/query" --data-urlencode "query=sum(rate(http_server_requests_seconds_count{status=\"429\"}[1m]))"
  fi
}

__query_fallbacks() {
  echo "fallback invocations in last 5m (gateway prometheus via Prometheus):"
  if command -v jq >/dev/null 2>&1; then
    curl -sG "${PROM_URL}/api/v1/query" --data-urlencode "query=increase(gateway_fallback_invocations_total[5m])" | jq -r '.data.result'
  else
    curl -sG "${PROM_URL}/api/v1/query" --data-urlencode "query=increase(gateway_fallback_invocations_total[5m])"
  fi
}

main() {
  # Run test
  __get_token
  __fire_burst

  # Wait a few seconds for metrics to scrape
  sleep 15

  # Query metrics
  __query_gateway_metrics
  __query_429_rate
  __query_fallbacks

  echo "Test completed. If you saw many 429 HTTP codes and fallback invocations increased, the rate-limiter triggered as expected."
}

time main | tee result-rate-limit-test-"$(date +%Y%m%d-%H%M%S)".log
