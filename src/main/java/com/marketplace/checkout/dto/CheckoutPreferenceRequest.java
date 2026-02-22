package com.marketplace.checkout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CheckoutPreferenceRequest {

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<ItemRequest> items;

    @Valid
    private PayerRequest payer;

    @Valid
    private BackUrlsRequest backUrls;

    private String externalReference;
    private String notificationUrl;
    private Boolean autoReturn; // "approved" or "all"
    private Integer expirationDays;

    @Data
    public static class ItemRequest {
        @NotBlank(message = "Item ID is required")
        private String id;

        @NotBlank(message = "Item title is required")
        private String title;

        private String description;
        private String pictureUrl;
        private String categoryId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.01", message = "Unit price must be positive")
        private BigDecimal unitPrice;

        private String currencyId = "BRL";
    }

    @Data
    public static class PayerRequest {
        private String name;
        private String surname;
        private String email;
        private PhoneRequest phone;
        private AddressRequest address;

        @Data
        public static class PhoneRequest {
            private String areaCode;
            private String number;
        }

        @Data
        public static class AddressRequest {
            private String zipCode;
            private String streetName;
            private String streetNumber;
        }
    }

    @Data
    public static class BackUrlsRequest {
        private String success;
        private String failure;
        private String pending;
    }
}
