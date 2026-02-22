package com.marketplace.checkout.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.checkout.dto.CheckoutPreferenceRequest;
import com.marketplace.checkout.dto.CheckoutPreferenceResponse;
import com.marketplace.checkout.exception.CheckoutException;
import com.marketplace.checkout.service.CheckoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CheckoutController.class)
@DisplayName("CheckoutController")
class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CheckoutService checkoutService;

    private CheckoutPreferenceResponse buildResponse() {
        return CheckoutPreferenceResponse.builder()
                .id("PREF-123")
                .initPoint("https://www.mercadopago.com/checkout/PREF-123")
                .sandboxInitPoint("https://sandbox.mercadopago.com/checkout/PREF-123")
                .externalReference("ORDER-001")
                .dateCreated(OffsetDateTime.now())
                .status("regular_payment")
                .build();
    }

    private CheckoutPreferenceRequest buildValidRequest() {
        CheckoutPreferenceRequest req = new CheckoutPreferenceRequest();

        CheckoutPreferenceRequest.ItemRequest item = new CheckoutPreferenceRequest.ItemRequest();
        item.setId("ITEM-001");
        item.setTitle("Produto Teste");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("99.90"));

        req.setItems(List.of(item));
        req.setExternalReference("ORDER-001");
        return req;
    }

    @Nested
    @DisplayName("POST /api/checkout/preferences")
    class PostPreferences {

        @Test
        @DisplayName("deve retornar 201 e corpo da preferência criada")
        void deveRetornar201() throws Exception {
            when(checkoutService.createPreference(any())).thenReturn(buildResponse());

            mockMvc.perform(post("/api/checkout/preferences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildValidRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value("PREF-123"))
                    .andExpect(jsonPath("$.initPoint").isNotEmpty())
                    .andExpect(jsonPath("$.externalReference").value("ORDER-001"))
                    .andExpect(jsonPath("$.status").value("regular_payment"));
        }

        @Test
        @DisplayName("deve retornar 400 quando items está vazio")
        void deveRetornar400SemItems() throws Exception {
            CheckoutPreferenceRequest req = new CheckoutPreferenceRequest();
            req.setItems(List.of());

            mockMvc.perform(post("/api/checkout/preferences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Error"));
        }

        @Test
        @DisplayName("deve retornar 400 quando items é null")
        void deveRetornar400ItemsNull() throws Exception {
            CheckoutPreferenceRequest req = new CheckoutPreferenceRequest();

            mockMvc.perform(post("/api/checkout/preferences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 400 quando item não tem title")
        void deveRetornar400SemTitle() throws Exception {
            CheckoutPreferenceRequest req = new CheckoutPreferenceRequest();
            CheckoutPreferenceRequest.ItemRequest item = new CheckoutPreferenceRequest.ItemRequest();
            item.setId("ITEM-001");
            item.setQuantity(1);
            item.setUnitPrice(BigDecimal.TEN);
            req.setItems(List.of(item));

            mockMvc.perform(post("/api/checkout/preferences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasItem("Item title is required")));
        }

        @Test
        @DisplayName("deve retornar 400 quando item não tem id")
        void deveRetornar400SemId() throws Exception {
            CheckoutPreferenceRequest req = new CheckoutPreferenceRequest();
            CheckoutPreferenceRequest.ItemRequest item = new CheckoutPreferenceRequest.ItemRequest();
            item.setTitle("Produto");
            item.setQuantity(1);
            item.setUnitPrice(BigDecimal.TEN);
            req.setItems(List.of(item));

            mockMvc.perform(post("/api/checkout/preferences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasItem("Item ID is required")));
        }

        @Test
        @DisplayName("deve retornar 400 quando quantity é 0")
        void deveRetornar400QuantityZero() throws Exception {
            CheckoutPreferenceRequest req = new CheckoutPreferenceRequest();
            CheckoutPreferenceRequest.ItemRequest item = new CheckoutPreferenceRequest.ItemRequest();
            item.setId("ITEM-001");
            item.setTitle("Produto");
            item.setQuantity(0);
            item.setUnitPrice(BigDecimal.TEN);
            req.setItems(List.of(item));

            mockMvc.perform(post("/api/checkout/preferences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasItem("Quantity must be at least 1")));
        }

        @Test
        @DisplayName("deve retornar 400 quando unitPrice é zero")
        void deveRetornar400UnitPriceZero() throws Exception {
            CheckoutPreferenceRequest req = new CheckoutPreferenceRequest();
            CheckoutPreferenceRequest.ItemRequest item = new CheckoutPreferenceRequest.ItemRequest();
            item.setId("ITEM-001");
            item.setTitle("Produto");
            item.setQuantity(1);
            item.setUnitPrice(BigDecimal.ZERO);
            req.setItems(List.of(item));

            mockMvc.perform(post("/api/checkout/preferences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasItem("Unit price must be positive")));
        }

        @Test
        @DisplayName("deve retornar 400 quando quantity é null")
        void deveRetornar400QuantityNull() throws Exception {
            CheckoutPreferenceRequest req = new CheckoutPreferenceRequest();
            CheckoutPreferenceRequest.ItemRequest item = new CheckoutPreferenceRequest.ItemRequest();
            item.setId("ITEM-001");
            item.setTitle("Produto");
            item.setUnitPrice(BigDecimal.TEN);
            req.setItems(List.of(item));

            mockMvc.perform(post("/api/checkout/preferences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasItem("Quantity is required")));
        }

        @Test
        @DisplayName("deve retornar 400 quando unitPrice é null")
        void deveRetornar400UnitPriceNull() throws Exception {
            CheckoutPreferenceRequest req = new CheckoutPreferenceRequest();
            CheckoutPreferenceRequest.ItemRequest item = new CheckoutPreferenceRequest.ItemRequest();
            item.setId("ITEM-001");
            item.setTitle("Produto");
            item.setQuantity(1);
            req.setItems(List.of(item));

            mockMvc.perform(post("/api/checkout/preferences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details", hasItem("Unit price is required")));
        }

        @Test
        @DisplayName("deve propagar CheckoutException como erro de negócio")
        void devePropagrarCheckoutException() throws Exception {
            when(checkoutService.createPreference(any()))
                    .thenThrow(new CheckoutException("Mercado Pago API error: Unauthorized", 401));

            mockMvc.perform(post("/api/checkout/preferences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildValidRequest())))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Checkout Error"));
        }

        @Test
        @DisplayName("deve retornar 500 em caso de exceção genérica")
        void deveRetornar500EmExcecaoGenerica() throws Exception {
            when(checkoutService.createPreference(any()))
                    .thenThrow(new RuntimeException("Unexpected error"));

            mockMvc.perform(post("/api/checkout/preferences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildValidRequest())))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"));
        }
    }

    @Nested
    @DisplayName("GET /api/checkout/preferences/{id}")
    class GetPreferences {

        @Test
        @DisplayName("deve retornar 200 com a preferência pelo ID")
        void deveRetornar200() throws Exception {
            when(checkoutService.getPreference("PREF-123")).thenReturn(buildResponse());

            mockMvc.perform(get("/api/checkout/preferences/PREF-123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("PREF-123"))
                    .andExpect(jsonPath("$.initPoint").isNotEmpty());
        }

        @Test
        @DisplayName("deve retornar 404 quando preferência não existe")
        void deveRetornar404() throws Exception {
            when(checkoutService.getPreference("PREF-INVALID"))
                    .thenThrow(new CheckoutException("Preference not found", 404));

            mockMvc.perform(get("/api/checkout/preferences/PREF-INVALID"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("deve retornar 500 em caso de erro interno ao buscar preferência")
        void deveRetornar500NaFalhaDeGet() throws Exception {
            when(checkoutService.getPreference(any()))
                    .thenThrow(new CheckoutException("Error fetching preference", 500));

            mockMvc.perform(get("/api/checkout/preferences/PREF-ANY"))
                    .andExpect(status().isInternalServerError());
        }
    }
}
