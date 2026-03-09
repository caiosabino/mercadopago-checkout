package com.marketplace.checkout.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotBlank(message = "EAN is required")
    private String ean;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock must be >= 0")
    private Integer stock;

    @NotBlank(message = "Condition is required")
    private String condition;

    private List<String> images;

    @NotBlank(message = "Seller is required")
    private String seller;

    @NotNull(message = "freeShipping is required")
    private Boolean freeShipping;

    @NotNull(message = "weightKg is required")
    @DecimalMin(value = "0.001", message = "weightKg must be positive")
    private BigDecimal weightKg;

    @NotBlank(message = "Status is required")
    private String status;
}
