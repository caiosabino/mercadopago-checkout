package com.marketplace.checkout.controller;

import com.marketplace.checkout.dto.WebhookNotification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@Tag(name = "Webhooks", description = "Mercado Pago payment notifications")
public class WebhookController {

    /**
     * Endpoint para receber notificações IPN/Webhook do Mercado Pago.
     * Configure a URL deste endpoint no painel do Mercado Pago ou via notificationUrl na preferência.
     */
    @PostMapping("/mercadopago")
    @Operation(summary = "Receive Mercado Pago webhook notifications")
    public ResponseEntity<Void> receiveNotification(
            @RequestBody(required = false) WebhookNotification notification,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "topic", required = false) String topic) {

        // IPN (query params)
        if (id != null && topic != null) {
            log.info("IPN received - topic: {}, id: {}", topic, id);
            handleIpn(topic, id);
            return ResponseEntity.ok().build();
        }

        // Webhook (body)
        if (notification != null) {
            log.info("Webhook received - type: {}, action: {}, dataId: {}",
                    notification.getType(),
                    notification.getAction(),
                    notification.getData() != null ? notification.getData().getId() : "N/A");
            handleWebhook(notification);
        }

        return ResponseEntity.ok().build();
    }

    private void handleIpn(String topic, String id) {
        switch (topic) {
            case "payment" -> log.info("Payment notification received. Payment ID: {}", id);
            case "merchant_order" -> log.info("Merchant order notification. Order ID: {}", id);
            default -> log.warn("Unknown IPN topic: {}", topic);
        }
        // TODO: implement business logic — update order status, notify seller, etc.
    }

    private void handleWebhook(WebhookNotification notification) {
        if ("payment".equals(notification.getType())) {
            String paymentId = notification.getData() != null ? notification.getData().getId() : null;
            log.info("Payment webhook. Action: {}, PaymentID: {}", notification.getAction(), paymentId);
            // TODO: query payment details from MP API and update your order
        }
    }
}
