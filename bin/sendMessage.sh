#!/usr/bin/env bash
set -eu
file_name=${1:-"example1.json"}
environment=${2:-"sandbox"}
file="../src/main/resources/${file_name}"
echo "using file: ${file_name} environment:${environment}"
shared_access_signature="SharedAccessSignature sr=https%3A%2F%2Frd-servicebus-sandbox.servicebus.windows.net&sig=eaTe8E1mxcXhmMwmGmGhmlYhgPLFYPHBKJAwmavs44g%3D&se=1600945588&skn=SendAndListenSharedAccessKey"


curl -v \
 -d @${file}\
 -H "Authorization: ${shared_access_signature}"\
 -H "Content-Type: application/json"\
 "https://rd-servicebus-${environment}.servicebus.windows.net/rd-caseworker-topic-sandbox/messages"