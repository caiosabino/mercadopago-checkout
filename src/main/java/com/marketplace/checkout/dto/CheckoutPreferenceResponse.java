package com.marketplace.checkout.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class CheckoutPreferenceResponse {
    private String id;
    private String initPoint;        // URL de pagamento (produção)
    private String sandboxInitPoint; // URL de pagamento (sandbox)
    private String externalReference;
    private OffsetDateTime dateCreated;
    private OffsetDateTime expirationDateTo;
    private String status;
}
