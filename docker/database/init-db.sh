#!/usr/bin/env bash

set -e

if [ -z "$ORG_ROLE_MAPPING_DB_USERNAME" ] || [ -z "$ORG_ROLE_MAPPING_DB_PASSWORD" ]; then
  echo "ERROR: Missing environment variable. Set value for both 'ORG_ROLE_MAPPING_DB_USERNAME' and 'ORG_ROLE_MAPPING_DB_PASSWORD'."
  exit 1
fi

# Create role and database
psql -v ON_ERROR_STOP=1 --username postgres --set USERNAME=$ORG_ROLE_MAPPING_DB_USERNAME --set PASSWORD=$ORG_ROLE_MAPPING_DB_PASSWORD <<-EOSQL
  CREATE USER :USERNAME WITH PASSWORD ':PASSWORD';
  CREATE DATABASE org_role_mapping
    WITH OWNER = :USERNAME
    ENCODING = 'UTF-8'
    CONNECTION LIMIT = -1;
  ALTER SCHEMA public OWNER TO :USERNAME;

EOSQL
