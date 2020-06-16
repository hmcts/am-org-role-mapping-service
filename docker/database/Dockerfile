FROM postgres:11.8

COPY init-database.sh /docker-entrypoint-initdb.d

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD psql -c 'select 1' -d org_role_mapping -U ${ORG_ROLE_MAPPING_SERVICE_POSTGRES_USER}

EXPOSE 5432
