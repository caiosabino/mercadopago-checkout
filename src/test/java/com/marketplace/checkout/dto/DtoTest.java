package com.marketplace.checkout.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DTOs")
class DtoTest {

    @Test
    @DisplayName("CheckoutPreferenceResponse - builder deve preencher todos os campos")
    void checkoutPreferenceResponseBuilder() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiry = now.plusDays(3);

        CheckoutPreferenceResponse response = CheckoutPreferenceResponse.builder()
                .id("PREF-123")
                .initPoint("https://mp.com/checkout")
                .sandboxInitPoint("https://sandbox.mp.com/checkout")
                .externalReference("ORDER-001")
                .dateCreated(now)
                .expirationDateTo(expiry)
                .status("regular_payment")
                .build();

        assertThat(response.getId()).isEqualTo("PREF-123");
        assertThat(response.getInitPoint()).isEqualTo("https://mp.com/checkout");
        assertThat(response.getSandboxInitPoint()).isEqualTo("https://sandbox.mp.com/checkout");
        assertThat(response.getExternalReference()).isEqualTo("ORDER-001");
        assertThat(response.getDateCreated()).isEqualTo(now);
        assertThat(response.getExpirationDateTo()).isEqualTo(expiry);
        assertThat(response.getStatus()).isEqualTo("regular_payment");
    }

    @Test
    @DisplayName("CheckoutPreferenceRequest.ItemRequest - currencyId padrão é BRL")
    void itemRequestCurrencyIdDefault() {
        CheckoutPreferenceRequest.ItemRequest item = new CheckoutPreferenceRequest.ItemRequest();
        assertThat(item.getCurrencyId()).isEqualTo("BRL");
    }

    @Test
    @DisplayName("CheckoutPreferenceRequest.ItemRequest - setter e getter")
    void itemRequestSetterGetter() {
        CheckoutPreferenceRequest.ItemRequest item = new CheckoutPreferenceRequest.ItemRequest();
        item.setId("ITEM-1");
        item.setTitle("Livro");
        item.setDescription("Livro de Java");
        item.setPictureUrl("https://img.com/livro.jpg");
        item.setCategoryId("books");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("49.90"));
        item.setCurrencyId("USD");

        assertThat(item.getId()).isEqualTo("ITEM-1");
        assertThat(item.getTitle()).isEqualTo("Livro");
        assertThat(item.getDescription()).isEqualTo("Livro de Java");
        assertThat(item.getPictureUrl()).isEqualTo("https://img.com/livro.jpg");
        assertThat(item.getCategoryId()).isEqualTo("books");
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getUnitPrice()).isEqualByComparingTo("49.90");
        assertThat(item.getCurrencyId()).isEqualTo("USD");
    }

    @Test
    @DisplayName("CheckoutPreferenceRequest.PayerRequest - setter e getter completo")
    void payerRequestSetterGetter() {
        CheckoutPreferenceRequest.PayerRequest payer = new CheckoutPreferenceRequest.PayerRequest();
        payer.setName("Maria");
        payer.setSurname("Oliveira");
        payer.setEmail("maria@example.com");

        CheckoutPreferenceRequest.PayerRequest.PhoneRequest phone = new CheckoutPreferenceRequest.PayerRequest.PhoneRequest();
        phone.setAreaCode("21");
        phone.setNumber("988887777");
        payer.setPhone(phone);

        CheckoutPreferenceRequest.PayerRequest.AddressRequest address = new CheckoutPreferenceRequest.PayerRequest.AddressRequest();
        address.setZipCode("20040-020");
        address.setStreetName("Rua da Quitanda");
        address.setStreetNumber("50");
        payer.setAddress(address);

        assertThat(payer.getName()).isEqualTo("Maria");
        assertThat(payer.getSurname()).isEqualTo("Oliveira");
        assertThat(payer.getEmail()).isEqualTo("maria@example.com");
        assertThat(payer.getPhone().getAreaCode()).isEqualTo("21");
        assertThat(payer.getPhone().getNumber()).isEqualTo("988887777");
        assertThat(payer.getAddress().getZipCode()).isEqualTo("20040-020");
        assertThat(payer.getAddress().getStreetName()).isEqualTo("Rua da Quitanda");
        assertThat(payer.getAddress().getStreetNumber()).isEqualTo("50");
    }

    @Test
    @DisplayName("CheckoutPreferenceRequest.BackUrlsRequest - setter e getter")
    void backUrlsRequestSetterGetter() {
        CheckoutPreferenceRequest.BackUrlsRequest backUrls = new CheckoutPreferenceRequest.BackUrlsRequest();
        backUrls.setSuccess("https://app.com/success");
        backUrls.setFailure("https://app.com/failure");
        backUrls.setPending("https://app.com/pending");

        assertThat(backUrls.getSuccess()).isEqualTo("https://app.com/success");
        assertThat(backUrls.getFailure()).isEqualTo("https://app.com/failure");
        assertThat(backUrls.getPending()).isEqualTo("https://app.com/pending");
    }

    @Test
    @DisplayName("WebhookNotification - setter e getter completo")
    void webhookNotificationSetterGetter() {
        WebhookNotification notif = new WebhookNotification();
        notif.setId("NOTIF-1");
        notif.setAction("payment.updated");
        notif.setApiVersion("v1");
        notif.setLiveMode(false);
        notif.setType("payment");
        notif.setDateCreated("2024-01-01T00:00:00Z");
        notif.setUserId(123456L);

        WebhookNotification.DataInfo data = new WebhookNotification.DataInfo();
        data.setId("PAY-999");
        notif.setData(data);

        assertThat(notif.getId()).isEqualTo("NOTIF-1");
        assertThat(notif.getAction()).isEqualTo("payment.updated");
        assertThat(notif.getApiVersion()).isEqualTo("v1");
        assertThat(notif.getLiveMode()).isFalse();
        assertThat(notif.getType()).isEqualTo("payment");
        assertThat(notif.getDateCreated()).isEqualTo("2024-01-01T00:00:00Z");
        assertThat(notif.getUserId()).isEqualTo(123456L);
        assertThat(notif.getData().getId()).isEqualTo("PAY-999");
    }

    @Test
    @DisplayName("ErrorResponse - builder deve preencher todos os campos")
    void errorResponseBuilder() {
        OffsetDateTime now = OffsetDateTime.now();
        ErrorResponse error = ErrorResponse.builder()
                .status(400)
                .error("Validation Error")
                .message("Invalid data")
                .timestamp(java.time.LocalDateTime.now())
                .details(java.util.List.of("field is required"))
                .build();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getError()).isEqualTo("Validation Error");
        assertThat(error.getMessage()).isEqualTo("Invalid data");
        assertThat(error.getTimestamp()).isNotNull();
        assertThat(error.getDetails()).containsExactly("field is required");
    }
}
