package com.marketplace.checkout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PixPaymentRequest {

    @NotNull(message = "Transaction amount is required")
    @DecimalMin(value = "0.01", message = "Transaction amount must be positive")
    private BigDecimal transactionAmount;

    @NotBlank(message = "Description is required")
    private String description;

    @Valid
    @NotNull(message = "Payer is required")
    private PayerRequest payer;

    private String externalReference;
    private String txId;

    @Data
    public static class PayerRequest {
        @NotBlank(message = "Payer email is required")
        @Email(message = "Payer email must be valid")
        private String email;
        private String name;
        private String cpf;
        private String cnpj;
    }
}
