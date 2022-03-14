#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER $PRECERT_USER WITH PASSWORD '$PRECERT_PASSWORD';
    CREATE DATABASE $PRECERT_DB;
    GRANT ALL PRIVILEGES ON DATABASE $PRECERT_DB TO $PRECERT_USER;
    CREATE USER $CLAIMS_USER WITH PASSWORD '$CLAIMS_PASSWORD';
    CREATE DATABASE $CLAIMS_DB;
    GRANT ALL PRIVILEGES ON DATABASE $CLAIMS_DB TO $CLAIMS_USER;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$PRECERT_USER" --dbname "$PRECERT_DB" -f /scripts/create-schema-precert.sql
psql -v ON_ERROR_STOP=1 --username "$PRECERT_USER" --dbname "$PRECERT_DB" -f /scripts/load-data-precert.sql

psql -v ON_ERROR_STOP=1 --username "$CLAIMS_USER" --dbname "$CLAIMS_DB" -f /scripts/create-schema-claims.sql
psql -v ON_ERROR_STOP=1 --username "$CLAIMS_USER" --dbname "$CLAIMS_DB" -f /scripts/load-data-claims.sql