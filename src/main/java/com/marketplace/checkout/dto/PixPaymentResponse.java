package com.marketplace.checkout.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class PixPaymentResponse {
    private String paymentId;
    private String status;
    private String statusDetail;
    private String qrCode;
    private String qrCodeBase64;
    private String transactionId;
    private String ticketUrl;
    private OffsetDateTime dateCreated;
}
