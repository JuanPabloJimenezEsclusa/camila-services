#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${DEBUG:-}" == "true" ]]; then set -o xtrace; fi  # Enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

# JMeter installation variables
JMETER_VERSION="${JMETER_VERSION:-"5.6.3"}"
JMETER_INSTALL_DIR="/usr/local/bin"
JMETER_MIRROR="${JMETER_MIRROR:-"https://dlcdn.apache.org//jmeter/binaries"}"

# JMeter test configuration variables
JMETER_TEST_PATH="${JMETER_TEST_PATH:-"."}" # This variable defines the path to the JMeter test plan configuration
THREADS="${THREADS:-200}" # This variable sets the number of concurrent users (threads) to simulate during the test
RAMP_UP="${RAMP_UP:-20}" # This variable specifies the duration (in seconds) for gradually increasing the load from 0 to the specified number of users
LOOPS="${LOOPS:-10}" # This variable defines the total number of times to iterate through the test

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

__ensureJMeterInstalled() {
  echo -e "${SEPARATOR} 🔧 Ensuring JMeter is installed ${SEPARATOR}"
  if command -v jmeter &> /dev/null; then
    echo "✓ JMeter already installed: $(jmeter --version 2>&1 | head -n1)"
    return 0
  fi
  echo "JMeter not found. Installing JMeter ${JMETER_VERSION}..."

  local jmeter_archive="apache-jmeter-${JMETER_VERSION}.tgz"
  local jmeter_url="${JMETER_MIRROR}/${jmeter_archive}"
  temp_dir="$(mktemp -d)"
  trap 'rm -rf "${temp_dir}"' EXIT

  echo -e "${SEPARATOR} ⬇️ Downloading JMeter ${JMETER_VERSION} ${SEPARATOR}"
  if ! curl -fsSL "${jmeter_url}" -o "${temp_dir}/${jmeter_archive}"; then
    echo "Error: Failed to download JMeter" >&2
    return 1
  fi

  if curl -fsSL "${jmeter_url}.sha512" -o "${temp_dir}/${jmeter_archive}.sha512" 2>/dev/null; then
    echo "Verifying checksum..."
    cd "${temp_dir}"
    if command -v sha512sum &> /dev/null; then
      sha512sum -c "${jmeter_archive}.sha512" || { echo "Error: Checksum verification failed" >&2; return 1; }
    elif command -v shasum &> /dev/null; then
      shasum -a 512 -c "${jmeter_archive}.sha512" || { echo "Error: Checksum verification failed" >&2; return 1; }
    else
      echo "Warning: No SHA512 tool found, skipping checksum verification"
    fi
    cd - > /dev/null
  fi

  echo "Extracting JMeter..."
  tar -xzf "${temp_dir}/${jmeter_archive}" -C "${temp_dir}"
  local jmeter_dir="${temp_dir}/apache-jmeter-${JMETER_VERSION}"

  if [[ -d "${JMETER_INSTALL_DIR}/apache-jmeter-${JMETER_VERSION}" ]]; then
    sudo rm -rf "${JMETER_INSTALL_DIR}/apache-jmeter-${JMETER_VERSION}"
  fi

  sudo mv "${jmeter_dir}" "${JMETER_INSTALL_DIR}/"
  sudo ln -sf "${JMETER_INSTALL_DIR}/apache-jmeter-${JMETER_VERSION}/bin/jmeter" "${JMETER_INSTALL_DIR}/jmeter"
  sudo chmod +x "${JMETER_INSTALL_DIR}/jmeter"

  echo "✓ JMeter ${JMETER_VERSION} successfully installed to ${JMETER_INSTALL_DIR}"
  jmeter --version 2>&1 | head -n1
}

__cleanupWorkspace() {
  echo -e "${SEPARATOR} 🧹 Cleaning up workspace ${SEPARATOR}"
  rm -dr "${JMETER_TEST_PATH}/reports" || true
  rm -dr "${JMETER_TEST_PATH}/jmeter.log" || true
}

__testExecution() {
  echo -e "${SEPARATOR} 🚀 Executing JMeter test plan ${SEPARATOR}"
  jmeter -n \
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
}

# JMeter HTML Report Generation
__reportGeneration() {
  echo -e "${SEPARATOR} 📊 Generating JMeter HTML report ${SEPARATOR}"
  jmeter \
    -g "${JMETER_TEST_PATH}/reports/result.csv" \
    -o "${JMETER_TEST_PATH}/reports/html"
}

main() {
  echo -e "${SEPARATOR} 🏃‍♂️ Starting JMeter performance test ${SEPARATOR}"
  echo "JMETER_TEST_PATH: ${JMETER_TEST_PATH} | THREADS: ${THREADS} | RAMP_UP: ${RAMP_UP} | LOOPS: ${LOOPS}"
  echo "BASE_URL: ${BASE_URL_PROTOCOL}://${BASE_URL}:${BASE_URL_PORT}/${BASE_PATH}"
  echo "OAUTH_URL: ${OAUTH_URL_PROTOCOL}://${OAUTH_URL}:${OAUTH_URL_PORT}${OAUTH_PATH}"

  __ensureJMeterInstalled
  __cleanupWorkspace
  __testExecution
  __reportGeneration
}

echo -e "${SEPARATOR} 🔨 Main: ${0} ${SEPARATOR}"
time main "$@"
