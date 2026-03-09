#!/usr/bin/env bash
set -euo pipefail

# Generates Pix OAuth token (client_credentials) and optionally writes it to .env.
# Usage:
#   ./scripts/get-pix-token.sh
#   ./scripts/get-pix-token.sh --write-env
#   ./scripts/get-pix-token.sh --write-env --cert /path/client.crt --key /path/client.key

WRITE_ENV=false
CERT_FILE=""
KEY_FILE=""
ENV_FILE="${ENV_FILE:-.env}"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --write-env)
      WRITE_ENV=true
      shift
      ;;
    --cert)
      CERT_FILE="${2:-}"
      shift 2
      ;;
    --key)
      KEY_FILE="${2:-}"
      shift 2
      ;;
    --env-file)
      ENV_FILE="${2:-}"
      shift 2
      ;;
    -h|--help)
      sed -n '1,20p' "$0"
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      exit 1
      ;;
  esac
done

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Env file not found: $ENV_FILE" >&2
  exit 1
fi

read_env_var() {
  local key="$1"
  local line
  line="$(grep -E "^${key}=" "$ENV_FILE" | tail -n 1 || true)"
  if [[ -z "$line" ]]; then
    echo ""
    return 0
  fi

  local value="${line#*=}"
  if [[ "$value" =~ ^\".*\"$ ]]; then
    value="${value:1:${#value}-2}"
  fi
  echo "$value"
}

PIX_GOV_OAUTH_TOKEN_URL="$(read_env_var PIX_GOV_OAUTH_TOKEN_URL)"
PIX_GOV_CLIENT_ID="$(read_env_var PIX_GOV_CLIENT_ID)"
PIX_GOV_CLIENT_SECRET="$(read_env_var PIX_GOV_CLIENT_SECRET)"
PIX_GOV_SCOPE="$(read_env_var PIX_GOV_SCOPE)"

required=(PIX_GOV_OAUTH_TOKEN_URL PIX_GOV_CLIENT_ID PIX_GOV_CLIENT_SECRET PIX_GOV_SCOPE)
for var in "${required[@]}"; do
  if [[ -z "${!var:-}" ]]; then
    echo "Missing required env var: $var" >&2
    exit 1
  fi
done

CURL_ARGS=(
  --silent
  --show-error
  --fail
  --request POST "$PIX_GOV_OAUTH_TOKEN_URL"
  --user "$PIX_GOV_CLIENT_ID:$PIX_GOV_CLIENT_SECRET"
  --header "Content-Type: application/x-www-form-urlencoded"
  --data "grant_type=client_credentials&scope=$PIX_GOV_SCOPE"
)

if [[ -n "$CERT_FILE" || -n "$KEY_FILE" ]]; then
  if [[ -z "$CERT_FILE" || -z "$KEY_FILE" ]]; then
    echo "Use --cert and --key together" >&2
    exit 1
  fi
  CURL_ARGS+=(--cert "$CERT_FILE" --key "$KEY_FILE")
fi

response="$(curl "${CURL_ARGS[@]}")"

if command -v jq >/dev/null 2>&1; then
  access_token="$(printf '%s' "$response" | jq -r '.access_token // empty')"
  expires_in="$(printf '%s' "$response" | jq -r '.expires_in // empty')"
  token_type="$(printf '%s' "$response" | jq -r '.token_type // empty')"
else
  access_token="$(printf '%s' "$response" | sed -n 's/.*"access_token"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
  expires_in="$(printf '%s' "$response" | sed -n 's/.*"expires_in"[[:space:]]*:[[:space:]]*\([0-9]*\).*/\1/p')"
  token_type="$(printf '%s' "$response" | sed -n 's/.*"token_type"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
fi

if [[ -z "$access_token" ]]; then
  echo "Could not extract access_token from response:" >&2
  echo "$response" >&2
  exit 1
fi

echo "Token generated successfully"
if [[ -n "$token_type" ]]; then
  echo "token_type: $token_type"
fi
if [[ -n "$expires_in" ]]; then
  echo "expires_in: $expires_in"
fi
echo "access_token: $access_token"

if [[ "$WRITE_ENV" == true ]]; then
  escaped_token="$(printf '%s' "$access_token" | sed 's/[\/&]/\\&/g')"

  if grep -q '^PIX_GOV_BEARER_TOKEN=' "$ENV_FILE"; then
    sed -i "s/^PIX_GOV_BEARER_TOKEN=.*/PIX_GOV_BEARER_TOKEN=$escaped_token/" "$ENV_FILE"
  else
    printf '\nPIX_GOV_BEARER_TOKEN=%s\n' "$access_token" >> "$ENV_FILE"
  fi

  if grep -q '^PIX_GOV_MOCK_ENABLED=' "$ENV_FILE"; then
    sed -i 's/^PIX_GOV_MOCK_ENABLED=.*/PIX_GOV_MOCK_ENABLED=false/' "$ENV_FILE"
  else
    printf 'PIX_GOV_MOCK_ENABLED=false\n' >> "$ENV_FILE"
  fi

  echo "Updated $ENV_FILE with PIX_GOV_BEARER_TOKEN and PIX_GOV_MOCK_ENABLED=false"
fi
