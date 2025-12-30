#!/bin/bash

# Script to run the application with the GraalVM Native Image agent
# This script automatically captures all necessary configurations

set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

dirname="$(dirname "$0")"
cd "${dirname}/../../camila-product-api-infrastructure/driving/camila-product-api-infrastructure-boot" || exit 1

APP_NAME="camila-product-api-infrastructure-boot"
VERSION="1.0.0"
JAR_FILE="target/${APP_NAME}-${VERSION}.jar"
CONFIG_DIR="src/main/resources/META-INF/native-image"
TEMP_CONFIG_DIR="/tmp/native-image-config-$$"

echo "=================================================="
echo "GraalVM Native Image Agent - Configuration Capture"
echo "=================================================="
echo ""

# Verify that the JAR file exists
if [ ! -f "${JAR_FILE}" ]; then
  echo "Error: JAR file not found: ${JAR_FILE}"
  echo "Please run first: mvn clean package"
  exit 1
fi

# Verify that GraalVM is installed
if ! command -v native-image &> /dev/null; then
  echo "Warning: GraalVM native-image is not installed or not in PATH"
  echo "The agent will still work, but native-image will not be available"
fi

# Create temporary directory for configurations
mkdir -p "${TEMP_CONFIG_DIR}"

echo "Configuration:"
echo "  JAR: ${JAR_FILE}"
echo "  Config Dir: ${CONFIG_DIR}"
echo "  Temp Dir: ${TEMP_CONFIG_DIR}"
echo ""

# Agent options
AGENT_OPTIONS="config-output-dir=${TEMP_CONFIG_DIR}"

# If configurations already exist, merge them
if [ -d "${CONFIG_DIR}" ] && [ "$(ls -A ${CONFIG_DIR}/*.json 2>/dev/null)" ]; then
  echo "Existing configurations found. Will merge."
  AGENT_OPTIONS="config-merge-dir=${TEMP_CONFIG_DIR}"
  # Copy existing configurations to temporary directory
  cp "${CONFIG_DIR}"/*.json "${TEMP_CONFIG_DIR}/" 2>/dev/null || true
fi

echo ""
echo "=================================================="
echo "Starting application with tracing agent..."
echo "=================================================="
echo ""
echo "IMPORTANT:"
echo "1. The application will start with the agent active"
echo "2. Exercise ALL endpoints and functionalities"
echo "3. Press Ctrl+C when you're done"
echo ""

# shellcheck disable=SC2162
read -p "Press Enter to continue..."

echo "Starting dependent services with Docker Compose..."
docker compose -f ../../../../camila-orchestrator/dev/compose/docker-compose-spring-boot.yml up -d

echo "Run the application with the agent"
java -agentlib:native-image-agent=$AGENT_OPTIONS \
  -Dspring.profiles.active=loc \
  -jar "$JAR_FILE"

JAVA_EXIT_CODE=$?

echo "Stopping dependent services... (${JAVA_EXIT_CODE})"
docker compose -f ../../../../camila-orchestrator/dev/compose/docker-compose-spring-boot.yml down

echo ""
echo "=================================================="
echo "Application stopped"
echo "=================================================="
echo ""

if [ ${JAVA_EXIT_CODE} -eq 0 ] || [ ${JAVA_EXIT_CODE} -eq 130 ]; then
  # 130 = Ctrl+C
  # Verify that configurations were generated
  if [ "$(ls -A ${TEMP_CONFIG_DIR}/*.json 2>/dev/null)" ]; then
    echo "✓ Configurations captured successfully"
    echo ""
    echo "Generated files:"
    ls -lh "$TEMP_CONFIG_DIR"/*.json
    echo ""

    mkdir -p "$CONFIG_DIR"
    echo "Copying configurations to $CONFIG_DIR..."
    cp "$TEMP_CONFIG_DIR"/*.json "$CONFIG_DIR/"

    echo ""
    echo "✓ Configurations copied successfully"
    echo ""
    echo "Configuration summary:"
    echo "----------------------------"

    for config_file in "$CONFIG_DIR"/*.json; do
      if [ -f "$config_file" ]; then
        filename=$(basename "$config_file")
        size=$(wc -l < "$config_file")
        echo "  $filename: $size lines"
      fi
    done

    echo ""
    echo "=================================================="
    echo "Next steps:"
    echo "=================================================="
    echo "1. Review the generated configurations in:"
    echo "   ${CONFIG_DIR}"
    echo ""
    echo "2. Build the native image:"
    echo "   ./../build-image-native.sh"
    echo ""
    echo "3. If you encounter errors, run this script again"
    echo "   and exercise more functionalities"
    echo ""
  else
    echo "⚠ Warning: No configuration files were generated"
    echo "The application may not have executed correctly"
  fi
else
  echo "Error: The application exited with code ${JAVA_EXIT_CODE}"
  exit ${JAVA_EXIT_CODE}
fi

# Clean up temporary directory
rm -rf "${TEMP_CONFIG_DIR}" 2>/dev/null || true

echo "✓ Process completed"
