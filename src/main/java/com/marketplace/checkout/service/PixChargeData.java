package com.marketplace.checkout.service;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class PixChargeData {
    private String txId;
    private String status;
    private String qrCode;
    private String qrCodeBase64;
    private String location;
    private OffsetDateTime createdAt;
}
