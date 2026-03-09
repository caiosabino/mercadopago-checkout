#!/usr/bin/env bash
set -euo pipefail

API_URL="${API_URL:-http://localhost:8080}"
ENDPOINT="$API_URL/api/checkout/pix/payments"

PAYLOAD='{
  "transactionAmount": 49.90,
  "description": "Pagamento pedido ORDER-PIX-LOCAL-1",
  "externalReference": "ORDER-PIX-LOCAL-1",
  "payer": {
    "email": "cliente.teste@example.com"
  }
}'

echo "POST $ENDPOINT"
curl --silent --show-error --location \
  --request POST "$ENDPOINT" \
  --header 'Content-Type: application/json' \
  --data "$PAYLOAD"

echo
