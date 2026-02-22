package com.marketplace.checkout.service;

import com.marketplace.checkout.dto.CheckoutPreferenceRequest;
import com.marketplace.checkout.dto.CheckoutPreferenceResponse;
import com.marketplace.checkout.exception.CheckoutException;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutService")
class CheckoutServiceTest {

    @InjectMocks
    private CheckoutService checkoutService;

    private CheckoutPreferenceRequest buildMinimalRequest() {
        CheckoutPreferenceRequest req = new CheckoutPreferenceRequest();

        CheckoutPreferenceRequest.ItemRequest item = new CheckoutPreferenceRequest.ItemRequest();
        item.setId("ITEM-001");
        item.setTitle("Produto Teste");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("99.90"));
        item.setCurrencyId("BRL");

        req.setItems(List.of(item));
        req.setExternalReference("ORDER-001");
        return req;
    }

    private Preference buildMockPreference() {
        Preference pref = mock(Preference.class);
        when(pref.getId()).thenReturn("PREF-123");
        when(pref.getInitPoint()).thenReturn("https://www.mercadopago.com/checkout/PREF-123");
        when(pref.getSandboxInitPoint()).thenReturn("https://sandbox.mercadopago.com/checkout/PREF-123");
        when(pref.getExternalReference()).thenReturn("ORDER-001");
        when(pref.getDateCreated()).thenReturn(OffsetDateTime.now());
        when(pref.getExpirationDateTo()).thenReturn(OffsetDateTime.now().plusDays(3));

        return pref;
    }

    @Nested
    @DisplayName("createPreference")
    class CreatePreference {

        @Test
        @DisplayName("deve criar preferência com sucesso - request mínimo")
        void deveCrearPreferenciaMinima() throws Exception {
            Preference mockPref = buildMockPreference();

            try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                    (mock, ctx) -> when(mock.create(any())).thenReturn(mockPref))) {

                CheckoutPreferenceResponse response = checkoutService.createPreference(buildMinimalRequest());

                assertThat(response).isNotNull();
                assertThat(response.getId()).isEqualTo("PREF-123");
                assertThat(response.getInitPoint()).contains("PREF-123");
                assertThat(response.getSandboxInitPoint()).contains("PREF-123");
                assertThat(response.getExternalReference()).isEqualTo("ORDER-001");
            }
        }

        @Test
        @DisplayName("deve criar preferência com payer completo")
        void deveCrearPreferenceComPayerCompleto() throws Exception {
            CheckoutPreferenceRequest req = buildMinimalRequest();

            CheckoutPreferenceRequest.PayerRequest payer = new CheckoutPreferenceRequest.PayerRequest();
            payer.setName("João");
            payer.setSurname("Silva");
            payer.setEmail("joao@example.com");

            CheckoutPreferenceRequest.PayerRequest.PhoneRequest phone = new CheckoutPreferenceRequest.PayerRequest.PhoneRequest();
            phone.setAreaCode("11");
            phone.setNumber("999999999");
            payer.setPhone(phone);

            CheckoutPreferenceRequest.PayerRequest.AddressRequest address = new CheckoutPreferenceRequest.PayerRequest.AddressRequest();
            address.setZipCode("01310-100");
            address.setStreetName("Avenida Paulista");
            address.setStreetNumber("1000");
            payer.setAddress(address);

            req.setPayer(payer);

            Preference mockPref = buildMockPreference();

            try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                    (mock, ctx) -> when(mock.create(any())).thenReturn(mockPref))) {

                CheckoutPreferenceResponse response = checkoutService.createPreference(req);
                assertThat(response.getId()).isEqualTo("PREF-123");
            }
        }

        @Test
        @DisplayName("deve criar preferência com backUrls")
        void deveCrearPreferenceComBackUrls() throws Exception {
            CheckoutPreferenceRequest req = buildMinimalRequest();

            CheckoutPreferenceRequest.BackUrlsRequest backUrls = new CheckoutPreferenceRequest.BackUrlsRequest();
            backUrls.setSuccess("https://app.com/success");
            backUrls.setFailure("https://app.com/failure");
            backUrls.setPending("https://app.com/pending");
            req.setBackUrls(backUrls);
            req.setAutoReturn(true);

            Preference mockPref = buildMockPreference();

            try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                    (mock, ctx) -> when(mock.create(any())).thenReturn(mockPref))) {

                CheckoutPreferenceResponse response = checkoutService.createPreference(req);
                assertThat(response).isNotNull();
            }
        }

        @Test
        @DisplayName("deve usar notificationUrl do request quando fornecida")
        void deveUsarNotificationUrlDoRequest() throws Exception {
            CheckoutPreferenceRequest req = buildMinimalRequest();
            req.setNotificationUrl("https://myapp.com/webhook");

            Preference mockPref = buildMockPreference();

            try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                    (mock, ctx) -> when(mock.create(any())).thenReturn(mockPref))) {

                CheckoutPreferenceResponse response = checkoutService.createPreference(req);
                assertThat(response).isNotNull();
            }
        }

        @Test
        @DisplayName("deve usar defaultNotificationUrl quando request não tem url")
        void deveUsarDefaultNotificationUrl() throws Exception {
            ReflectionTestUtils.setField(checkoutService, "defaultNotificationUrl", "https://default.com/webhook");

            Preference mockPref = buildMockPreference();

            try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                    (mock, ctx) -> when(mock.create(any())).thenReturn(mockPref))) {

                CheckoutPreferenceResponse response = checkoutService.createPreference(buildMinimalRequest());
                assertThat(response).isNotNull();
            }
        }

        @Test
        @DisplayName("deve criar preferência com múltiplos itens")
        void deveCrearPreferenceComMultiplosItens() throws Exception {
            CheckoutPreferenceRequest req = new CheckoutPreferenceRequest();

            CheckoutPreferenceRequest.ItemRequest item1 = new CheckoutPreferenceRequest.ItemRequest();
            item1.setId("ITEM-001");
            item1.setTitle("Produto 1");
            item1.setQuantity(2);
            item1.setUnitPrice(new BigDecimal("50.00"));

            CheckoutPreferenceRequest.ItemRequest item2 = new CheckoutPreferenceRequest.ItemRequest();
            item2.setId("ITEM-002");
            item2.setTitle("Produto 2");
            item2.setDescription("Descrição do produto 2");
            item2.setPictureUrl("https://img.com/produto2.jpg");
            item2.setCategoryId("electronics");
            item2.setQuantity(1);
            item2.setUnitPrice(new BigDecimal("150.00"));

            req.setItems(List.of(item1, item2));

            Preference mockPref = buildMockPreference();

            try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                    (mock, ctx) -> when(mock.create(any())).thenReturn(mockPref))) {

                CheckoutPreferenceResponse response = checkoutService.createPreference(req);
                assertThat(response).isNotNull();
            }
        }

        @Test
        @DisplayName("deve lançar CheckoutException quando MPApiException ocorre")
        void deveLancarCheckoutExceptionParaMPApiException() throws Exception {
            MPApiException mpApiEx = mock(MPApiException.class);
            when(mpApiEx.getStatusCode()).thenReturn(401);
            when(mpApiEx.getMessage()).thenReturn("Unauthorized");

            try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                    (mock, ctx) -> when(mock.create(any())).thenThrow(mpApiEx))) {

                assertThatThrownBy(() -> checkoutService.createPreference(buildMinimalRequest()))
                        .isInstanceOf(CheckoutException.class)
                        .hasMessageContaining("Mercado Pago API error")
                        .extracting("statusCode").isEqualTo(401);
            }
        }

        @Test
        @DisplayName("deve lançar CheckoutException com status 500 quando MPException ocorre")
        void deveLancarCheckoutExceptionParaMPException() throws Exception {
            MPException mpEx = mock(MPException.class);
            when(mpEx.getMessage()).thenReturn("SDK error");

            try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                    (mock, ctx) -> when(mock.create(any())).thenThrow(mpEx))) {

                assertThatThrownBy(() -> checkoutService.createPreference(buildMinimalRequest()))
                        .isInstanceOf(CheckoutException.class)
                        .hasMessageContaining("Mercado Pago SDK error")
                        .extracting("statusCode").isEqualTo(500);
            }
        }

        @Test
        @DisplayName("deve criar preferência com autoReturn false")
        void deveCrearPreferenceAutoReturnFalse() throws Exception {
            CheckoutPreferenceRequest req = buildMinimalRequest();
            req.setAutoReturn(false);

            Preference mockPref = buildMockPreference();

            try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                    (mock, ctx) -> when(mock.create(any())).thenReturn(mockPref))) {

                CheckoutPreferenceResponse response = checkoutService.createPreference(req);
                assertThat(response).isNotNull();
            }
        }

        @Test
        @DisplayName("deve criar preferência com autoReturn null")
        void deveCrearPreferenceAutoReturnNull() throws Exception {
            CheckoutPreferenceRequest req = buildMinimalRequest();
            req.setAutoReturn(null);

            Preference mockPref = buildMockPreference();

            try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                    (mock, ctx) -> when(mock.create(any())).thenReturn(mockPref))) {

                CheckoutPreferenceResponse response = checkoutService.createPreference(req);
                assertThat(response).isNotNull();
            }
        }

        @Test
        @DisplayName("deve criar preferência com payer sem phone e sem address")
        void deveCrearPreferenceComPayerSemPhoneESemAddress() throws Exception {
            CheckoutPreferenceRequest req = buildMinimalRequest();

            CheckoutPreferenceRequest.PayerRequest payer = new CheckoutPreferenceRequest.PayerRequest();
            payer.setEmail("user@test.com");
            req.setPayer(payer);

            Preference mockPref = buildMockPreference();

            try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                    (mock, ctx) -> when(mock.create(any())).thenReturn(mockPref))) {

                CheckoutPreferenceResponse response = checkoutService.createPreference(req);
                assertThat(response).isNotNull();
            }
        }

        @Test
        @DisplayName("deve criar preferência sem notificationUrl (ambas em branco)")
        void deveCrearPreferenceSemNotificationUrl() throws Exception {
            ReflectionTestUtils.setField(checkoutService, "defaultNotificationUrl", "");
            CheckoutPreferenceRequest req = buildMinimalRequest();
            req.setNotificationUrl(null);

            Preference mockPref = buildMockPreference();

            try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                    (mock, ctx) -> when(mock.create(any())).thenReturn(mockPref))) {

                CheckoutPreferenceResponse response = checkoutService.createPreference(req);
                assertThat(response).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("getPreference")
    class GetPreference {

        @Test
        @DisplayName("deve retornar preferência pelo ID com sucesso")
        void deveRetornarPreferenciaPorId() throws Exception {
            Preference mockPref = buildMockPreference();

            try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                    (mock, ctx) -> when(mock.get("PREF-123")).thenReturn(mockPref))) {

                CheckoutPreferenceResponse response = checkoutService.getPreference("PREF-123");

                assertThat(response).isNotNull();
                assertThat(response.getId()).isEqualTo("PREF-123");
                assertThat(response.getInitPoint()).isNotBlank();
                assertThat(response.getSandboxInitPoint()).isNotBlank();
                assertThat(response.getExternalReference()).isEqualTo("ORDER-001");
            }
        }

        @Test
        @DisplayName("deve lançar CheckoutException quando preferência não encontrada (MPApiException)")
        void deveLancarExceptionQuandoPrefNaoEncontrada() throws Exception {
            MPApiException mpApiEx = mock(MPApiException.class);
            when(mpApiEx.getStatusCode()).thenReturn(404);
            when(mpApiEx.getMessage()).thenReturn("Not Found");

            try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                    (mock, ctx) -> when(mock.get(any())).thenThrow(mpApiEx))) {

                assertThatThrownBy(() -> checkoutService.getPreference("PREF-INVALID"))
                        .isInstanceOf(CheckoutException.class)
                        .hasMessageContaining("Preference not found")
                        .extracting("statusCode").isEqualTo(404);
            }
        }

        @Test
        @DisplayName("deve lançar CheckoutException com status 500 em MPException no getPreference")
        void deveLancarExceptionMPExceptionNoGet() throws Exception {
            MPException mpEx = mock(MPException.class);
            when(mpEx.getMessage()).thenReturn("connection error");

            try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                    (mock, ctx) -> when(mock.get(any())).thenThrow(mpEx))) {

                assertThatThrownBy(() -> checkoutService.getPreference("PREF-ANY"))
                        .isInstanceOf(CheckoutException.class)
                        .hasMessageContaining("Error fetching preference")
                        .extracting("statusCode").isEqualTo(500);
            }
        }
    }
}
