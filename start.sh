#!/usr/bin/env bash
FLAG='CHANGE_ME'

set -euo pipefail

if [[ $# -ne 1 || ! $1 =~ ^[0-9]{1,5}$ ]]; then
  echo "Usage: ./start.sh PORT" >&2
  exit 2
fi

port=$((10#$1))
if (( port < 1 || port > 65535 )); then
  echo "Port must be between 1 and 65535." >&2
  exit 2
fi

cd "$(dirname "$0")"
configuration="$PWD/.env"
umask 077

if [[ ! -e $configuration ]]; then
  if [[ $FLAG == CHANGE_ME ]]; then
    echo "Set FLAG at the top of start.sh before running." >&2
    exit 2
  fi
  db_password=$(openssl rand -hex 32)
  db_root_password=$(openssl rand -hex 32)
  temporary=$(mktemp "$PWD/.env.XXXXXX")
  trap 'rm -f "$temporary"' EXIT
  printf 'FLAG=%s\nDB_PASSWORD=%s\nDB_ROOT_PASSWORD=%s\nAPP_PORT=%s\n' \
    "$FLAG" "$db_password" "$db_root_password" "$port" > "$temporary"
  if ! ln "$temporary" "$configuration" 2>/dev/null && [[ ! -f $configuration ]]; then
    echo "Could not create $configuration" >&2
    exit 1
  fi
  rm -f "$temporary"
  trap - EXIT
fi

if [[ ! -f $configuration || -L $configuration ]]; then
  echo "Invalid configuration file: $configuration" >&2
  exit 1
fi
chmod 600 "$configuration"

unset FLAG DB_PASSWORD DB_ROOT_PASSWORD
APP_PORT="$port" docker compose up -d --build
