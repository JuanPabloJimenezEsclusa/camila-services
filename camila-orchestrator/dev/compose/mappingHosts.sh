#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR

SEPARATOR="\n ################################################## \n"

source_str='127.0.0.1  admin  config  discovery  gateway  prometheus  grafana  zipkin  keycloak  mongodb  couchbase  backend-product  compose-backend-product-1  compose-backend-product-2  compose-backend-product-3  elasticsearch  kibana  fluentd  redis  kafka'
sed -i -e "\|$source_str|h;"      `# Search for the source string and copy it to the hold space` \
    -e "\${"                      `# Go to the end of the file and run the following commands` \
    -e "x;"                       `# Exchange the last line with the hold space` \
    -e "s|$source_str||;"         `# Erase the source string if it was actually found` \
    -e "{g;"                      `# Bring back the last line` \
    -e "t};"                      `# Test if the substitution succeeded (the source string was found)` \
    -e "a\\" -e "$source_str"     `# Append the source string if we didn't move to the next cycle` \
    -e "}" /etc/hosts

echo -e "${SEPARATOR} 🔨 Mapping hosts.... ${SEPARATOR}"
cat /etc/hosts
