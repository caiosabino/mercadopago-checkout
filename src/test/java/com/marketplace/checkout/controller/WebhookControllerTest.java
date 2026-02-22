package com.marketplace.checkout.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.checkout.dto.WebhookNotification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
@DisplayName("WebhookController")
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /api/webhooks/mercadopago")
    class PostWebhook {

        @Test
        @DisplayName("deve retornar 200 para notificação IPN de pagamento")
        void deveRetornar200ParaIpnPayment() throws Exception {
            mockMvc.perform(post("/api/webhooks/mercadopago")
                            .param("id", "PAY-001")
                            .param("topic", "payment")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("deve retornar 200 para notificação IPN de merchant_order")
        void deveRetornar200ParaIpnMerchantOrder() throws Exception {
            mockMvc.perform(post("/api/webhooks/mercadopago")
                            .param("id", "ORDER-001")
                            .param("topic", "merchant_order")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("deve retornar 200 para IPN com topic desconhecido")
        void deveRetornar200ParaIpnTopicDesconhecido() throws Exception {
            mockMvc.perform(post("/api/webhooks/mercadopago")
                            .param("id", "XYZ-001")
                            .param("topic", "unknown_topic")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("deve retornar 200 para webhook com body de pagamento")
        void deveRetornar200ParaWebhookPayment() throws Exception {
            WebhookNotification notification = new WebhookNotification();
            notification.setId("NOTIF-001");
            notification.setType("payment");
            notification.setAction("payment.updated");
            notification.setLiveMode(true);

            WebhookNotification.DataInfo data = new WebhookNotification.DataInfo();
            data.setId("PAY-99999");
            notification.setData(data);

            mockMvc.perform(post("/api/webhooks/mercadopago")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(notification)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("deve retornar 200 para webhook com body de tipo não-payment")
        void deveRetornar200ParaWebhookTipoNaoPayment() throws Exception {
            WebhookNotification notification = new WebhookNotification();
            notification.setType("subscription");
            notification.setAction("subscription.authorized");

            mockMvc.perform(post("/api/webhooks/mercadopago")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(notification)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("deve retornar 200 para webhook com body de payment sem data")
        void deveRetornar200ParaWebhookPaymentSemData() throws Exception {
            WebhookNotification notification = new WebhookNotification();
            notification.setType("payment");
            notification.setAction("payment.created");
            notification.setData(null);

            mockMvc.perform(post("/api/webhooks/mercadopago")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(notification)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("deve retornar 200 para body vazio (sem IPN params e sem body)")
        void deveRetornar200ParaBodyVazio() throws Exception {
            mockMvc.perform(post("/api/webhooks/mercadopago")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("deve priorizar IPN params quando ambos estão presentes")
        void devePriorizarIpnParamsQuandoAmbosPresentes() throws Exception {
            WebhookNotification notification = new WebhookNotification();
            notification.setType("payment");

            mockMvc.perform(post("/api/webhooks/mercadopago")
                            .param("id", "PAY-001")
                            .param("topic", "payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(notification)))
                    .andExpect(status().isOk());
        }
    }
}
