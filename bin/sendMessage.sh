#!/usr/bin/env bash
set -eu
file_name=${1:-"example1.json"}
environment=${2:-"aat"}
file="../src/main/resources/${file_name}"
echo "using file: ${file_name} environment:${environment}"
shared_access_signature="SharedAccessSignature sr=sb%3A%2F%2Frd-servicebus-aat.servicebus.windows.net&sig=okr8Zwd2SSKXSbeTB1coc3rMlP6PafVndathZwCKbZI%3D&se=1608230338&skn=SendAndListenSharedAccessKey"


curl -v \
 -d @${file}\
 -H "Authorization: ${shared_access_signature}"\
 -H "Content-Type: application/json"\
 "https://rd-servicebus-${environment}.servicebus.windows.net/rd-caseworker-topic-aat/messages"