# 🛒 Checkout — Spring Boot

Aplicação Spring Boot 3.4 / Java 21 / Gradle para integração com:
- **Checkout Preferences** do Mercado Pago
- **Pix API padrão Bacen (governo)** via PSP compatível

## Pré-requisitos

- Java 21+
- Gradle 8+
- Conta no [Mercado Pago Developers](https://www.mercadopago.com.br/developers)
- Access Token (Sandbox ou Produção)

---

## ⚙️ Configuração

### 1. Obtenha seu Access Token

Acesse o painel do Mercado Pago Developers > Suas integrações > Credenciais.

- **Sandbox:** `TEST-xxxxxxxxxxxx`
- **Produção:** `APP_USR-xxxxxxxxxxxx`

### 2. Configure as variáveis de ambiente

```bash
export MERCADOPAGO_ACCESS_TOKEN=TEST-seu-token-aqui
export MERCADOPAGO_NOTIFICATION_URL=https://seu-dominio.com/api/webhooks/checkout
export PIX_GOV_BASE_URL=https://seu-psp-pix.com.br
export PIX_GOV_RECEIVER_KEY=sua-chave-pix
export PIX_GOV_BEARER_TOKEN=token-ou-use-oauth
export PIX_GOV_MOCK_ENABLED=false
```

Ou edite `src/main/resources/application.yml`:

```yaml
mercadopago:
  access-token: TEST-seu-token-aqui
  notification-url: https://seu-dominio.com/api/webhooks/checkout
pix:
  gov:
    mock-enabled: false
    base-url: https://seu-psp-pix.com.br
    receiver-key: sua-chave-pix
    bearer-token: token-ou-use-oauth
```

---

## 🚀 Executar

```bash
./gradlew bootRun
```

Acesse o Swagger UI: http://localhost:8080/swagger-ui.html

---

## 📡 Endpoints

### POST `/api/checkout/preferences`
Cria uma preferência de checkout no Mercado Pago.

**Request body:**
```json
{
  "items": [
    {
      "id": "PROD-001",
      "title": "Camiseta Azul",
      "description": "Camiseta manga curta tamanho M",
      "pictureUrl": "https://example.com/img/camiseta.jpg",
      "categoryId": "fashion",
      "quantity": 2,
      "unitPrice": 89.90,
      "currencyId": "BRL"
    }
  ],
  "payer": {
    "name": "João",
    "surname": "Silva",
    "email": "joao@email.com",
    "phone": { "areaCode": "11", "number": "999999999" },
    "address": {
      "zipCode": "01310-100",
      "streetName": "Av. Paulista",
      "streetNumber": "1000"
    }
  },
  "backUrls": {
    "success": "https://seu-marketplace.com/pagamento/sucesso",
    "failure": "https://seu-marketplace.com/pagamento/erro",
    "pending": "https://seu-marketplace.com/pagamento/pendente"
  },
  "externalReference": "ORDER-12345",
  "autoReturn": true
}
```

**Response:**
```json
{
  "id": "123456789-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "initPoint": "https://www.mercadopago.com.br/checkout/v1/redirect?pref_id=...",
  "sandboxInitPoint": "https://sandbox.mercadopago.com.br/checkout/v1/redirect?pref_id=...",
  "externalReference": "ORDER-12345",
  "dateCreated": "2025-01-01T12:00:00Z",
  "status": "regular_payment"
}
```

### GET `/api/checkout/preferences/{id}`
Busca uma preferência existente pelo ID.

### POST `/api/checkout/pix/payments`
Cria cobrança Pix (API padrão Bacen) e retorna dados de QR Code.

**Request body:**
```json
{
  "transactionAmount": 49.90,
  "description": "Pagamento pedido ORDER-PIX-1",
  "externalReference": "ORDER-PIX-1",
  "txId": "txid-opcional-123",
  "payer": {
    "name": "Cliente Teste",
    "cpf": "12345678909"
  }
}
```

### POST `/api/webhooks/checkout`
Recebe notificações de pagamento do Mercado Pago (IPN / Webhook).

### POST `/api/webhooks/mercadopago`
Alias legado para compatibilidade do webhook.

### Products (`/products`)

CRUD de produtos do marketplace.

### GET `/products`
Lista produtos com filtros opcionais:

- `search` (texto livre em `title`, `description`, `sku`, `brand`)
- `category` (match exato)
- `status` (default `all`; ex.: `active`, `inactive`)

Exemplo:

```bash
curl "http://localhost:8080/products?search=notebook&category=Eletronicos&status=active"
```

### POST `/products`
Cria um produto.

**Request body:**
```json
{
  "title": "Notebook Gamer 16GB",
  "description": "Tela 15.6, SSD 512GB",
  "price": 5999.90,
  "category": "Eletronicos",
  "brand": "Marca X",
  "sku": "SKU-NOTE-001",
  "ean": "7891234567890",
  "stock": 12,
  "condition": "new",
  "images": [
    "https://cdn.exemplo.com/produtos/note-1.jpg"
  ],
  "seller": "Loja Oficial",
  "freeShipping": true,
  "weightKg": 2.35,
  "status": "active"
}
```

**Response (201):**
```json
{
  "id": "p-1a2b3c4d",
  "title": "Notebook Gamer 16GB",
  "description": "Tela 15.6, SSD 512GB",
  "price": 5999.90,
  "category": "Eletronicos",
  "brand": "Marca X",
  "sku": "SKU-NOTE-001",
  "ean": "7891234567890",
  "stock": 12,
  "condition": "new",
  "images": [
    "https://cdn.exemplo.com/produtos/note-1.jpg"
  ],
  "seller": "Loja Oficial",
  "freeShipping": true,
  "weightKg": 2.35,
  "status": "active",
  "createdAt": "2026-03-09T20:00:00-03:00",
  "updatedAt": "2026-03-09T20:00:00-03:00"
}
```

### PUT `/products/{id}`
Atualiza um produto existente. O payload é o mesmo do `POST /products`.

### DELETE `/products/{id}`
Remove um produto. Retorna `204 No Content`.

---

## 🌐 Integração com o Frontend (Marketplace)

### Opção 1 — Redirect (mais simples)
```javascript
// Chame seu backend para criar a preferência
const response = await fetch('https://sua-api.com/api/checkout/preferences', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ items: [...], payer: {...} })
});

const { sandboxInitPoint, initPoint } = await response.json();

// Redirecione o usuário para o checkout
window.location.href = sandboxInitPoint; // use initPoint em produção
```

### Opção 2 — Checkout Bricks (embutido na página)
```html
<div id="wallet_container"></div>

<script src="https://sdk.mercadopago.com/js/v2"></script>
<script>
  const mp = new MercadoPago('SUA_PUBLIC_KEY', { locale: 'pt-BR' });

  // Após obter o preferenceId do seu backend:
  const bricks = mp.bricks();
  bricks.create('wallet', 'wallet_container', {
    initialization: { preferenceId: 'PREFERENCE_ID_AQUI' }
  });
</script>
```

---

## 🔔 Webhooks

Configure a URL de notificação para receber atualizações de pagamento em tempo real.

1. No painel do Mercado Pago: Configurações > Notificações > IPN
2. URL principal: `https://seu-dominio.com/api/webhooks/checkout`
3. Compatibilidade legada: `https://seu-dominio.com/api/webhooks/mercadopago`
4. Para Pix Bacen, webhook é configurado no PSP via endpoint específico de webhook Pix.

---

## 🧪 Teste local Pix (curl)

Script pronto para ambiente de testes local:

```bash
./scripts/test-pix-local.sh
```

Ele chama `POST /api/checkout/pix/payments` em `http://localhost:8080`.
Por padrão, em ambiente local, `PIX_GOV_MOCK_ENABLED=true` e o endpoint responde com QR mock.

Implemente a lógica de negócio no `WebhookController.handleWebhook()`.

---

## .env

Configure um arquivo na raiz do projeto com o nome .env
e adicione as variáveis abaixo:

MERCADOPAGO_ACCESS_TOKEN
MERCADOPAGO_NOTIFICATION_URL
PIX_GOV_BASE_URL
PIX_GOV_RECEIVER_KEY
PIX_GOV_BEARER_TOKEN (ou PIX_GOV_OAUTH_TOKEN_URL + PIX_GOV_CLIENT_ID + PIX_GOV_CLIENT_SECRET)
USER
PASS

---

## 📦 Estrutura do Projeto

```
src/main/java/com/marketplace/checkout/
├── CheckoutApplication.java
├── config/
│   ├── MercadoPagoConfiguration.java  ← inicializa o SDK com o token
│   └── WebConfig.java                 ← CORS para o frontend
├── controller/
│   ├── CheckoutController.java        ← POST/GET /api/checkout/preferences
│   └── WebhookController.java         ← POST /api/webhooks/checkout (+ alias legado)
├── dto/
│   ├── CheckoutPreferenceRequest.java
│   ├── CheckoutPreferenceResponse.java
│   ├── PixPaymentRequest.java
│   ├── PixPaymentResponse.java
│   ├── WebhookNotification.java
│   └── ErrorResponse.java
├── exception/
│   ├── CheckoutException.java
│   └── GlobalExceptionHandler.java
└── service/
    └── CheckoutService.java           ← lógica de integração com o MP SDK
```
