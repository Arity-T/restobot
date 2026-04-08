#!/usr/bin/env bash
set -euo pipefail

: "${TARGET_HOST:?TARGET_HOST is required}"
: "${SSH_USER:?SSH_USER is required}"
: "${SSH_KEY_PATH:?SSH_KEY_PATH is required}"
: "${ARTIFACT_PATH:?ARTIFACT_PATH is required}"
: "${ENV_FILE_PATH:?ENV_FILE_PATH is required}"

APP_DIR="${APP_DIR:-/opt/restobot}"
APP_USER="${APP_USER:-restobot}"
SERVICE_NAME="${SERVICE_NAME:-restobot}"
APP_PORT="${APP_PORT:-8089}"
MAIN_DB_URL_FOR_VM="${MAIN_DB_URL_FOR_VM:-jdbc:postgresql://localhost:5432/main}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVICE_FILE_PATH="${SERVICE_FILE_PATH:-${SCRIPT_DIR}/restobot.service}"
MIGRATION_DIR_PATH="${MIGRATION_DIR_PATH:-${SCRIPT_DIR}/../logic/src/main/resources/db/migration/main}"
TMP_DIR="$(mktemp -d)"

cleanup() {
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

SSH_OPTS=(
  -i "$SSH_KEY_PATH"
  -o StrictHostKeyChecking=no
  -o UserKnownHostsFile=/dev/null
)

normalize_env() {
  perl -pe 's/\r$//' "$ENV_FILE_PATH" > "$TMP_DIR/restobot.env"

  if grep -q '^MAIN_DB_URL=' "$TMP_DIR/restobot.env"; then
    sed -i "s#^MAIN_DB_URL=.*#MAIN_DB_URL=${MAIN_DB_URL_FOR_VM}#" "$TMP_DIR/restobot.env"
  else
    echo "MAIN_DB_URL=${MAIN_DB_URL_FOR_VM}" >> "$TMP_DIR/restobot.env"
  fi

  if grep -q '^API_SERVER_HOST=' "$TMP_DIR/restobot.env"; then
    sed -i 's#^API_SERVER_HOST=.*#API_SERVER_HOST=0.0.0.0#' "$TMP_DIR/restobot.env"
  else
    echo 'API_SERVER_HOST=0.0.0.0' >> "$TMP_DIR/restobot.env"
  fi

  if grep -q '^API_SERVER_PORT=' "$TMP_DIR/restobot.env"; then
    sed -i "s#^API_SERVER_PORT=.*#API_SERVER_PORT=${APP_PORT}#" "$TMP_DIR/restobot.env"
  else
    echo "API_SERVER_PORT=${APP_PORT}" >> "$TMP_DIR/restobot.env"
  fi
}

normalize_env

cp "$ARTIFACT_PATH" "$TMP_DIR/app-fat.jar"
sed \
  -e "s#__APP_USER__#${APP_USER}#g" \
  -e "s#__APP_DIR__#${APP_DIR}#g" \
  "$SERVICE_FILE_PATH" > "$TMP_DIR/${SERVICE_NAME}.service"
cp "${MIGRATION_DIR_PATH}/V1__init_main.sql" "$TMP_DIR/V1__init_main.sql"
cp "${MIGRATION_DIR_PATH}/V2__add_data.sql" "$TMP_DIR/V2__add_data.sql"

scp "${SSH_OPTS[@]}" \
  "$TMP_DIR/app-fat.jar" \
  "$TMP_DIR/restobot.env" \
  "$TMP_DIR/${SERVICE_NAME}.service" \
  "$TMP_DIR/V1__init_main.sql" \
  "$TMP_DIR/V2__add_data.sql" \
  "${SSH_USER}@${TARGET_HOST}:/tmp/"

ssh "${SSH_OPTS[@]}" "${SSH_USER}@${TARGET_HOST}" \
  APP_DIR="$APP_DIR" \
  APP_USER="$APP_USER" \
  SERVICE_NAME="$SERVICE_NAME" \
  APP_PORT="$APP_PORT" \
  'bash -s' <<'EOF'
set -euo pipefail

sudo id -u "$APP_USER" >/dev/null 2>&1 || sudo useradd --system --create-home --shell /bin/bash "$APP_USER"
sudo install -d -o "$APP_USER" -g "$APP_USER" "$APP_DIR" "$APP_DIR/app"
sudo install -o "$APP_USER" -g "$APP_USER" -m 0644 /tmp/app-fat.jar "$APP_DIR/app/app-fat.jar"
sudo install -o "$APP_USER" -g "$APP_USER" -m 0600 /tmp/restobot.env "$APP_DIR/.env"
sudo install -m 0644 "/tmp/${SERVICE_NAME}.service" "/etc/systemd/system/${SERVICE_NAME}.service"

set -a
. /tmp/restobot.env
set +a
export PGPASSWORD="$MAIN_DB_PASSWORD"

if ! psql -h localhost -p 5432 -U "$MAIN_DB_USER" -d main -tAc "select 1" >/dev/null 2>&1; then
  psql -h localhost -p 5432 -U "$MAIN_DB_USER" -d postgres -tAc "select 1 from pg_database where datname = 'main'" | grep -q 1 || \
    psql -h localhost -p 5432 -U "$MAIN_DB_USER" -d postgres -c "create database main"
fi

CITY_EXISTS="$(psql -h localhost -p 5432 -U "$MAIN_DB_USER" -d main -tAc "select 1 from information_schema.tables where table_schema='public' and table_name='city'")"
CITY_EXISTS="$(echo "$CITY_EXISTS" | tr -d '[:space:]')"
if [ "$CITY_EXISTS" != "1" ]; then
  psql -h localhost -p 5432 -U "$MAIN_DB_USER" -d main -f /tmp/V1__init_main.sql
  psql -h localhost -p 5432 -U "$MAIN_DB_USER" -d main -f /tmp/V2__add_data.sql
fi

sudo systemctl daemon-reload
sudo systemctl enable "$SERVICE_NAME"
sudo systemctl restart "$SERVICE_NAME"
sudo systemctl --no-pager --full status "$SERVICE_NAME"

for _ in $(seq 1 20); do
  if curl -fsS "http://localhost:${APP_PORT}/healthcheck"; then
    exit 0
  fi
  sleep 3
done

echo "Application healthcheck failed after restart" >&2
exit 1
EOF
