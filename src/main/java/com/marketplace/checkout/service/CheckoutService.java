package com.marketplace.checkout.service;

import com.marketplace.checkout.dto.*;
import com.marketplace.checkout.exception.CheckoutException;
import com.mercadopago.client.common.AddressRequest;
import com.mercadopago.client.common.PhoneRequest;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.*;
import com.mercadopago.resources.preference.Preference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CheckoutService {

    @Value("${mercadopago.notification-url:}")
    private String defaultNotificationUrl;

    public CheckoutPreferenceResponse createPreference(CheckoutPreferenceRequest request) {
        log.info("Creating Mercado Pago preference for externalRef: {}", request.getExternalReference());

        try {
            PreferenceClient client = new PreferenceClient();

            PreferenceRequest mpRequest = buildPreferenceRequest(request);
            Preference preference = client.create(mpRequest);

            log.info("Preference created successfully. ID: {}", preference.getId());

            return CheckoutPreferenceResponse.builder()
                    .id(preference.getId())
                    .initPoint(preference.getInitPoint())
                    .sandboxInitPoint(preference.getSandboxInitPoint())
                    .externalReference(preference.getExternalReference())
                    .dateCreated(preference.getDateCreated())
                    .expirationDateTo(preference.getExpirationDateTo())
                    .status(preference.getOperationType())
                    .build();

        } catch (MPApiException ex) {
            log.error("Mercado Pago API error: status={}, message={}", ex.getStatusCode(), ex.getMessage());
            throw new CheckoutException(
                    "Mercado Pago API error: " + ex.getMessage(),
                    ex.getStatusCode(),
                    ex
            );
        } catch (MPException ex) {
            log.error("Mercado Pago SDK error: {}", ex.getMessage());
            throw new CheckoutException("Mercado Pago SDK error: " + ex.getMessage(), 500, ex);
        }
    }

    public CheckoutPreferenceResponse getPreference(String preferenceId) {
        log.info("Fetching preference: {}", preferenceId);
        try {
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.get(preferenceId);

            return CheckoutPreferenceResponse.builder()
                    .id(preference.getId())
                    .initPoint(preference.getInitPoint())
                    .sandboxInitPoint(preference.getSandboxInitPoint())
                    .externalReference(preference.getExternalReference())
                    .dateCreated(preference.getDateCreated())
                    .expirationDateTo(preference.getExpirationDateTo())
                    .build();

        } catch (MPApiException ex) {
            throw new CheckoutException("Preference not found: " + ex.getMessage(), ex.getStatusCode(), ex);
        } catch (MPException ex) {
            throw new CheckoutException("Error fetching preference: " + ex.getMessage(), 500, ex);
        }
    }

    // ---------------------------------------------------------------- builder

    private PreferenceRequest buildPreferenceRequest(CheckoutPreferenceRequest req) {
        var builder = PreferenceRequest.builder()
                .items(buildItems(req.getItems()))
                .externalReference(req.getExternalReference())
                .autoReturn(req.getAutoReturn() != null && req.getAutoReturn() ? "approved" : null);

        // Notification URL
        String notifUrl = (req.getNotificationUrl() != null && !req.getNotificationUrl().isBlank())
                ? req.getNotificationUrl()
                : defaultNotificationUrl;
        if (notifUrl != null && !notifUrl.isBlank()) {
            builder.notificationUrl(notifUrl);
        }

        // Payer
        if (req.getPayer() != null) {
            builder.payer(buildPayer(req.getPayer()));
        }

        // Back URLs
        if (req.getBackUrls() != null) {
            builder.backUrls(buildBackUrls(req.getBackUrls()));
        }

        return builder.build();
    }

    private List<PreferenceItemRequest> buildItems(List<CheckoutPreferenceRequest.ItemRequest> items) {
        return items.stream()
                .map(i -> PreferenceItemRequest.builder()
                        .id(i.getId())
                        .title(i.getTitle())
                        .description(i.getDescription())
                        .pictureUrl(i.getPictureUrl())
                        .categoryId(i.getCategoryId())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .currencyId(i.getCurrencyId())
                        .build())
                .toList();
    }

    private PreferencePayerRequest buildPayer(CheckoutPreferenceRequest.PayerRequest payer) {
        var builder = PreferencePayerRequest.builder()
                .name(payer.getName())
                .surname(payer.getSurname())
                .email(payer.getEmail());

        if (payer.getPhone() != null) {
            builder.phone(PhoneRequest.builder()
                    .areaCode(payer.getPhone().getAreaCode())
                    .number(payer.getPhone().getNumber())
                    .build());
        }

        if (payer.getAddress() != null) {
            builder.address(AddressRequest.builder()
                    .zipCode(payer.getAddress().getZipCode())
                    .streetName(payer.getAddress().getStreetName())
                    .streetNumber(payer.getAddress().getStreetNumber())
                    .build());
        }

        return builder.build();
    }

    private PreferenceBackUrlsRequest buildBackUrls(CheckoutPreferenceRequest.BackUrlsRequest backUrls) {
        return PreferenceBackUrlsRequest.builder()
                .success(backUrls.getSuccess())
                .failure(backUrls.getFailure())
                .pending(backUrls.getPending())
                .build();
    }
}
