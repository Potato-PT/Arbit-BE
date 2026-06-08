#!/bin/sh
set -eu

missing_vars=""

for var_name in APP_DB_URL APP_DB_USERNAME APP_DB_PASSWORD GCP_JWT_SECRET; do
  eval "var_value=\${$var_name:-}"
  if [ -z "$var_value" ]; then
    missing_vars="${missing_vars} ${var_name}"
  fi
done

if [ -n "$missing_vars" ]; then
  echo "Missing required environment variables:${missing_vars}" >&2
  exit 1
fi

echo "Starting Arbit with profile: ${SPRING_PROFILES_ACTIVE:-default}"

exec java -jar /app/app.jar
