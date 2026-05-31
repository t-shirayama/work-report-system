#!/usr/bin/env bash
set -euo pipefail

host="${SQLSERVER_HOST:-sqlserver}"
database="${SQLSERVER_DATABASE:-WorkReport}"
user="${SQLSERVER_USER:-sa}"
password="${MSSQL_SA_PASSWORD:-WorkReport!2026}"
seed_mode="${DB_SEED_MODE:-sample}"
sqlcmd="/opt/mssql-tools18/bin/sqlcmd"

echo "Waiting for SQL Server at ${host}..."
for _ in $(seq 1 60); do
  if "${sqlcmd}" -C -S "${host}" -U "${user}" -P "${password}" -Q "SELECT 1" >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

"${sqlcmd}" -C -S "${host}" -U "${user}" -P "${password}" -Q "IF DB_ID('${database}') IS NULL CREATE DATABASE [${database}];"

initialized="$("${sqlcmd}" -C -S "${host}" -U "${user}" -P "${password}" -d "${database}" -h -1 -W -Q "SET NOCOUNT ON; SELECT CASE WHEN OBJECT_ID('dbo.departments', 'U') IS NULL THEN 0 ELSE 1 END;")"

if [ "${initialized}" = "1" ]; then
  echo "Database ${database} is already initialized. Skipping schema and seed."
  exit 0
fi

echo "Applying schema.sql to ${database}..."
"${sqlcmd}" -C -S "${host}" -U "${user}" -P "${password}" -d "${database}" -i /sql/schema.sql

case "${seed_mode}" in
  sample)
    seed_file="/sql/seed.sql"
    ;;
  empty)
    seed_file="/sql/seed-empty.sql"
    ;;
  none)
    seed_file=""
    ;;
  *)
    echo "Unsupported DB_SEED_MODE: ${seed_mode}. Use sample, empty, or none."
    exit 1
    ;;
esac

if [ -n "${seed_file}" ]; then
  echo "Applying ${seed_file} to ${database}..."
  "${sqlcmd}" -C -S "${host}" -U "${user}" -P "${password}" -d "${database}" -i "${seed_file}"
else
  echo "Skipping seed data because DB_SEED_MODE=none."
fi

echo "Database ${database} initialization completed."
