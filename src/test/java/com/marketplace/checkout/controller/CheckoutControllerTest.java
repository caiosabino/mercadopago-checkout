package com.marketplace.checkout.controller;

import com.marketplace.checkout.dto.CheckoutPreferenceResponse;
import com.marketplace.checkout.dto.PixPaymentResponse;
import com.marketplace.checkout.exception.CheckoutException;
import com.marketplace.checkout.exception.GlobalExceptionHandler;
import com.marketplace.checkout.service.CheckoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutController")
class CheckoutControllerTest {

    @Mock
    private CheckoutService checkoutService;

    @InjectMocks
    private CheckoutController checkoutController;

    private MockMvc mockMvc;
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(checkoutController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("deve criar preferência e retornar 201")
    void deveCriarPreferenciaERetornarCreated() throws Exception {
        CheckoutPreferenceResponse response = CheckoutPreferenceResponse.builder()
                .id("PREF-123")
                .initPoint("https://mp.com/checkout/PREF-123")
                .sandboxInitPoint("https://sandbox.mp.com/checkout/PREF-123")
                .externalReference("ORDER-1")
                .dateCreated(OffsetDateTime.now())
                .expirationDateTo(OffsetDateTime.now().plusDays(1))
                .build();

        when(checkoutService.createPreference(any())).thenReturn(response);

        String payload = """
                {
                  "items": [
                    {
                      "id": "ITEM-1",
                      "title": "Produto",
                      "quantity": 1,
                      "unitPrice": 100.0
                    }
                  ],
                  "externalReference": "ORDER-1"
                }
                """;

        mockMvc.perform(post("/api/checkout/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("PREF-123"))
                .andExpect(jsonPath("$.externalReference").value("ORDER-1"));

        verify(checkoutService).createPreference(any());
    }

    @Test
    @DisplayName("deve retornar 400 quando request for inválido")
    void deveRetornar400QuandoRequestInvalido() throws Exception {
        mockMvc.perform(post("/api/checkout/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.details[0]").value("At least one item is required"));

        verify(checkoutService, never()).createPreference(any());
    }

    @Test
    @DisplayName("deve buscar preferência por id")
    void deveBuscarPreferenciaPorId() throws Exception {
        CheckoutPreferenceResponse response = CheckoutPreferenceResponse.builder()
                .id("PREF-123")
                .initPoint("https://mp.com/checkout/PREF-123")
                .build();

        when(checkoutService.getPreference("PREF-123")).thenReturn(response);

        mockMvc.perform(get("/api/checkout/preferences/PREF-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("PREF-123"));

        verify(checkoutService).getPreference("PREF-123");
    }

    @Test
    @DisplayName("deve mapear CheckoutException no GET")
    void deveMapearCheckoutExceptionNoGet() throws Exception {
        when(checkoutService.getPreference("PREF-404"))
                .thenThrow(new CheckoutException("Preferência não encontrada", 404));

        mockMvc.perform(get("/api/checkout/preferences/PREF-404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Checkout Error"))
                .andExpect(jsonPath("$.message").value("Preferência não encontrada"));
    }

    @Test
    @DisplayName("deve criar pagamento pix e retornar 201")
    void deveCriarPagamentoPixERetornarCreated() throws Exception {
        PixPaymentResponse response = PixPaymentResponse.builder()
                .paymentId("txid-123")
                .status("ATIVA")
                .statusDetail("active_charge")
                .qrCode("000201010212...")
                .qrCodeBase64("iVBORw0KGgoAAA...")
                .transactionId("txid-123")
                .build();

        when(checkoutService.createPixPayment(any())).thenReturn(response);

        String payload = """
                {
                  "transactionAmount": 34.50,
                  "description": "Pagamento pedido ORDER-PIX-1",
                  "externalReference": "ORDER-PIX-1",
                  "payer": {
                    "email": "pix@example.com"
                  }
                }
                """;

        mockMvc.perform(post("/api/checkout/pix/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value("txid-123"))
                .andExpect(jsonPath("$.status").value("ATIVA"))
                .andExpect(jsonPath("$.qrCode").value("000201010212..."));

        verify(checkoutService).createPixPayment(any());
    }

    @Test
    @DisplayName("deve retornar 400 quando request pix for inválido")
    void deveRetornar400QuandoRequestPixInvalido() throws Exception {
        String payload = """
                {
                  "transactionAmount": 0,
                  "payer": {}
                }
                """;

        mockMvc.perform(post("/api/checkout/pix/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));

        verify(checkoutService, never()).createPixPayment(any());
    }
}
