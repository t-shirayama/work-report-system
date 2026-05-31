#!/usr/bin/env bash
set -euo pipefail

host="${SQLSERVER_HOST:-sqlserver}"
database="${SQLSERVER_DATABASE:-WorkReport}"
user="${SQLSERVER_USER:-sa}"
password="${MSSQL_SA_PASSWORD:-WorkReport!2026}"
sqlcmd="/opt/mssql-tools18/bin/sqlcmd"

echo "Waiting for SQL Server at ${host}..."
for _ in $(seq 1 60); do
  if "${sqlcmd}" -C -S "${host}" -U "${user}" -P "${password}" -Q "SELECT 1" >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

echo "Dropping database ${database}..."
"${sqlcmd}" -C -S "${host}" -U "${user}" -P "${password}" -Q "IF DB_ID('${database}') IS NOT NULL BEGIN ALTER DATABASE [${database}] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE [${database}]; END;"

echo "Reinitializing database ${database}..."
/bin/bash /sql/init-db.sh
