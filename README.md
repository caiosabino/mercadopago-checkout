# 🛒 Mercado Pago Checkout — Spring Boot

Aplicação Spring Boot 3.4 / Java 21 / Gradle para integração com a API de **Checkout Preferences** do Mercado Pago.

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
export MERCADOPAGO_NOTIFICATION_URL=https://seu-dominio.com/api/webhooks/mercadopago
```

Ou edite `src/main/resources/application.yml`:

```yaml
mercadopago:
  access-token: TEST-seu-token-aqui
  notification-url: https://seu-dominio.com/api/webhooks/mercadopago
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

### POST `/api/webhooks/mercadopago`
Recebe notificações de pagamento do Mercado Pago (IPN / Webhook).

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
2. URL: `https://seu-dominio.com/api/webhooks/mercadopago`
3. Ou passe `notificationUrl` no corpo da requisição de criação da preferência.

Implemente a lógica de negócio no `WebhookController.handleWebhook()`.

---

## .env

Configure um arquivo na raiz do projeto com o nome .env
e adicione as variáveis abaixo:

MERCADOPAGO_ACCESS_TOKEN
MERCADOPAGO_NOTIFICATION_URL
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
│   └── WebhookController.java         ← POST /api/webhooks/mercadopago
├── dto/
│   ├── CheckoutPreferenceRequest.java
│   ├── CheckoutPreferenceResponse.java
│   ├── WebhookNotification.java
│   └── ErrorResponse.java
├── exception/
│   ├── CheckoutException.java
│   └── GlobalExceptionHandler.java
└── service/
    └── CheckoutService.java           ← lógica de integração com o MP SDK
```
