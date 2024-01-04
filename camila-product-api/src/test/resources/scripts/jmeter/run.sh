#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
set -o xtrace

JMETER_TEST_PATH="${JMETER_TEST_PATH:-"."}" # ruta de la configuración del plan de pruebas
THREADS="${THREADS:-100}" # hilos (usuarios) a utilizar
RAMP_UP="${RAMP_UP:-20}" # duración (en segundos) entre el inicio de la prueba y la carga máxima
LOOPS="${LOOPS:-5}" # total de iteraciones a ejecutar

echo "JMETER_TEST_PATH: ${JMETER_TEST_PATH} | THREADS: ${THREADS} | RAMP_UP: ${RAMP_UP} | LOOPS: ${LOOPS}"

# limpiar el espacio de trabajo
rm -dr "${JMETER_TEST_PATH}/reports" || true
rm -dr "${JMETER_TEST_PATH}/jmeter.log" || true

# ejecución via consola
time jmeter -n \
  -t "${JMETER_TEST_PATH}/camila-product-api.jmx" \
  -JTHREADS="${THREADS}" \
  -JRAMP_UP="${RAMP_UP}" \
  -JLOOPS="${LOOPS}" \
  -l "${JMETER_TEST_PATH}/reports/result.csv"

# generar reporte html
time jmeter \
  -g "${JMETER_TEST_PATH}/reports/result.csv" \
  -o "${JMETER_TEST_PATH}/reports/html"
