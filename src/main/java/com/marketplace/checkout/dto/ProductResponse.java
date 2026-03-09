package com.marketplace.checkout.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class ProductResponse {
    private String id;
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private String brand;
    private String sku;
    private String ean;
    private Integer stock;
    private String condition;
    private List<String> images;
    private String seller;
    private Boolean freeShipping;
    private BigDecimal weightKg;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
