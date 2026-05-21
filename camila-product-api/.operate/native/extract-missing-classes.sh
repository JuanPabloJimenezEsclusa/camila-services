#!/bin/bash

# Script para extraer clases que necesitan --initialize-at-build-time de los logs de GraalVM
# Uso: ./extract-missing-classes.sh build-log.txt

if [ $# -eq 0 ]; then
    echo "Uso: $0 <archivo-log>"
    echo "Ejemplo: $0 result-build-image-native.log"
    exit 1
fi

LOG_FILE="$1"

if [ ! -f "$LOG_FILE" ]; then
    echo "Error: El archivo $LOG_FILE no existe"
    exit 1
fi

echo "=================================================="
echo "Extrayendo clases para --initialize-at-build-time"
echo "=================================================="
echo ""

# Extraer clases que necesitan build-time initialization
BUILD_TIME_CLASSES=$(grep -oP "(?<=--initialize-at-build-time=)[^\s']+" "$LOG_FILE" | sort -u)

if [ -z "$BUILD_TIME_CLASSES" ]; then
    echo "No se encontraron clases que requieran build-time initialization"
else
    echo "Clases encontradas:"
    echo "$BUILD_TIME_CLASSES"
    echo ""
    echo "XML para pom.xml:"
    echo "---------------------------------------------------"
    while IFS= read -r class; do
        echo "                <buildArg>--initialize-at-build-time=$class</buildArg>"
    done <<< "$BUILD_TIME_CLASSES"
fi

echo ""
echo "=================================================="
echo "Extrayendo clases para --initialize-at-run-time"
echo "=================================================="
echo ""

# Extraer clases que necesitan run-time initialization
RUN_TIME_CLASSES=$(grep -oP "(?<=--initialize-at-run-time=)[^\s']+" "$LOG_FILE" | sort -u)

if [ -z "$RUN_TIME_CLASSES" ]; then
    echo "No se encontraron clases que requieran run-time initialization"
else
    echo "Clases encontradas:"
    echo "$RUN_TIME_CLASSES"
    echo ""
    echo "XML para pom.xml:"
    echo "---------------------------------------------------"
    while IFS= read -r class; do
        echo "                <buildArg>--initialize-at-run-time=$class</buildArg>"
    done <<< "$RUN_TIME_CLASSES"
fi

echo ""
echo "=================================================="
echo "Resumen de errores"
echo "=================================================="
echo ""

# Contar tipos de errores
UNSUPPORTED_FEATURE=$(grep -c "UnsupportedFeatureException" "$LOG_FILE" || echo "0")
PARSE_ERRORS=$(grep -c "Error parsing.*configuration" "$LOG_FILE" || echo "0")
MISSING_METADATA=$(grep -c "missing metadata" "$LOG_FILE" || echo "0")

echo "Total de UnsupportedFeatureException: $UNSUPPORTED_FEATURE"
echo "Total de errores de parsing: $PARSE_ERRORS"
echo "Total de metadata faltante: $MISSING_METADATA"

echo ""
echo "Archivo de análisis completo generado: analysis-report.txt"

{
    echo "========================================"
    echo "Reporte de Análisis de Build Nativo"
    echo "Fecha: $(date)"
    echo "========================================"
    echo ""
    echo "BUILD-TIME INITIALIZATION CLASSES:"
    echo "$BUILD_TIME_CLASSES"
    echo ""
    echo "RUN-TIME INITIALIZATION CLASSES:"
    echo "$RUN_TIME_CLASSES"
    echo ""
    echo "ERRORES ÚNICOS:"
    grep "Error:" "$LOG_FILE" | sort -u
} > analysis-report.txt

echo "Archivo generado exitosamente: analysis-report.txt"
