package com.marketplace.checkout.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("WebhookController")
class WebhookControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WebhookController()).build();
    }

    @Test
    @DisplayName("deve processar IPN via query params e retornar 200")
    void deveProcessarIpnViaQueryParams() throws Exception {
        mockMvc.perform(post("/api/webhooks/checkout")
                        .param("topic", "payment")
                        .param("id", "123456"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("deve processar webhook via body e retornar 200 na rota checkout")
    void deveProcessarWebhookViaBody() throws Exception {
        String payload = """
                {
                  "type": "payment",
                  "action": "payment.updated",
                  "data": {
                    "id": "999"
                  }
                }
                """;

        mockMvc.perform(post("/api/webhooks/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("deve retornar 200 quando notificação vier vazia")
    void deveRetornar200QuandoNotificacaoVazia() throws Exception {
        mockMvc.perform(post("/api/webhooks/checkout"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("deve manter compatibilidade na rota legada mercadopago")
    void deveManterCompatibilidadeRotaLegada() throws Exception {
        mockMvc.perform(post("/api/webhooks/mercadopago"))
                .andExpect(status().isOk());
    }
}
