package com.marketplace.checkout.service;

import com.marketplace.checkout.domain.entities.PreferenceEntity;
import com.marketplace.checkout.domain.repository.PreferenceRepository;
import com.marketplace.checkout.dto.*;
import com.marketplace.checkout.exception.CheckoutException;
import com.mercadopago.client.common.AddressRequest;
import com.mercadopago.client.common.PhoneRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.*;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService {

    @Value("${mercadopago.notification-url:}")
    private String defaultNotificationUrl;

    @Value("${pix.provider:mercadopago}")
    private String pixProvider;

    private final PreferenceClient client;
    private final PaymentClient paymentClient;
    private final PixGovClient pixGovClient;
    private final PreferenceRepository preferenceRepository;

    public CheckoutPreferenceResponse createPreference(CheckoutPreferenceRequest request) {
        log.info("Creating Mercado Pago preference for externalRef: {}", request.getExternalReference());

        try {
            PreferenceRequest mpRequest = buildPreferenceRequest(request);
            Preference preference = client.create(mpRequest);

            log.info("Preference created successfully. ID: {}", preference.getId());

            if (request.getPayer() != null) {
                preferenceRepository.save(
                        PreferenceEntity.builder()
                                .name(request.getPayer().getName())
                                .surName(request.getPayer().getSurname())
                                .preferenceId(preference.getId())
                                .build());
            }

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

    public PixPaymentResponse createPixPayment(PixPaymentRequest request) {
        String provider = pixProvider == null ? "mercadopago" : pixProvider.trim().toLowerCase();
        log.info("Creating Pix payment with provider={} for externalRef={}", provider, request.getExternalReference());

        if ("gov".equals(provider)) {
            try {
                PixChargeData chargeData = pixGovClient.createImmediateCharge(request);
                return PixPaymentResponse.builder()
                        .paymentId(chargeData.getTxId())
                        .status(chargeData.getStatus())
                        .statusDetail("active_charge")
                        .qrCode(chargeData.getQrCode())
                        .qrCodeBase64(chargeData.getQrCodeBase64())
                        .transactionId(chargeData.getTxId())
                        .ticketUrl(chargeData.getLocation())
                        .dateCreated(chargeData.getCreatedAt())
                        .build();
            } catch (CheckoutException ex) {
                throw ex;
            } catch (RestClientResponseException ex) {
                log.error("Pix Gov API error: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
                throw new CheckoutException("Pix API error: " + ex.getResponseBodyAsString(), ex.getStatusCode().value(), ex);
            } catch (RestClientException ex) {
                log.error("Pix Gov integration error: {}", ex.getMessage());
                throw new CheckoutException("Pix integration error: " + ex.getMessage(), 500, ex);
            }
        }

        return createPixPaymentMercadoPago(request);
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

    private PixPaymentResponse createPixPaymentMercadoPago(PixPaymentRequest request) {
        try {
            String notifUrl = defaultNotificationUrl != null && !defaultNotificationUrl.isBlank()
                    ? defaultNotificationUrl
                    : null;

            PaymentCreateRequest paymentRequest = PaymentCreateRequest.builder()
                    .paymentMethodId("pix")
                    .transactionAmount(request.getTransactionAmount())
                    .description(request.getDescription())
                    .externalReference(request.getExternalReference())
                    .payer(PaymentPayerRequest.builder()
                            .email(request.getPayer().getEmail())
                            .build())
                    .notificationUrl(notifUrl)
                    .build();

            Payment payment = paymentClient.create(paymentRequest);
            var transactionData = payment.getPointOfInteraction() != null
                    ? payment.getPointOfInteraction().getTransactionData()
                    : null;

            return PixPaymentResponse.builder()
                    .paymentId(payment.getId() != null ? payment.getId().toString() : null)
                    .status(payment.getStatus())
                    .statusDetail(payment.getStatusDetail())
                    .qrCode(transactionData != null ? transactionData.getQrCode() : null)
                    .qrCodeBase64(transactionData != null ? transactionData.getQrCodeBase64() : null)
                    .transactionId(transactionData != null ? transactionData.getTransactionId() : null)
                    .ticketUrl(transactionData != null ? transactionData.getTicketUrl() : null)
                    .dateCreated(payment.getDateCreated())
                    .build();
        } catch (MPApiException ex) {
            log.error("Mercado Pago Pix API error: status={}, message={}", ex.getStatusCode(), ex.getMessage());
            throw new CheckoutException("Mercado Pago API error: " + ex.getMessage(), ex.getStatusCode(), ex);
        } catch (MPException ex) {
            log.error("Mercado Pago Pix SDK error: {}", ex.getMessage());
            throw new CheckoutException("Mercado Pago SDK error: " + ex.getMessage(), 500, ex);
        }
    }

}
