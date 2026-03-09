package com.marketplace.checkout.service;

import com.marketplace.checkout.domain.entities.ProductEntity;
import com.marketplace.checkout.domain.repository.ProductRepository;
import com.marketplace.checkout.dto.ProductRequest;
import com.marketplace.checkout.dto.ProductResponse;
import com.marketplace.checkout.exception.CheckoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponse> listProducts(String search, String category, String status) {
        String searchTerm = search == null ? "" : search.trim().toLowerCase();
        String categoryTerm = category == null ? "" : category.trim();
        String statusTerm = status == null ? "all" : status.trim().toLowerCase();

        return productRepository.findAll().stream()
                .filter(product -> matchesSearch(product, searchTerm))
                .filter(product -> categoryTerm.isBlank() || product.getCategory().equals(categoryTerm))
                .filter(product -> "all".equals(statusTerm) || product.getStatus().equalsIgnoreCase(statusTerm))
                .sorted(Comparator.comparing(ProductEntity::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse createProduct(ProductRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        ProductEntity entity = new ProductEntity();
        entity.setId("p-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        applyRequest(entity, request);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        return toResponse(productRepository.save(entity));
    }

    public ProductResponse updateProduct(String id, ProductRequest request) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new CheckoutException("Produto nao encontrado", 404));

        applyRequest(entity, request);
        entity.setUpdatedAt(OffsetDateTime.now());
        return toResponse(productRepository.save(entity));
    }

    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new CheckoutException("Produto nao encontrado", 404);
        }
        productRepository.deleteById(id);
    }

    private boolean matchesSearch(ProductEntity product, String term) {
        if (term.isBlank()) {
            return true;
        }

        return product.getTitle().toLowerCase().contains(term)
                || product.getDescription().toLowerCase().contains(term)
                || product.getSku().toLowerCase().contains(term)
                || product.getBrand().toLowerCase().contains(term);
    }

    private void applyRequest(ProductEntity entity, ProductRequest request) {
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setPrice(request.getPrice());
        entity.setCategory(request.getCategory());
        entity.setBrand(request.getBrand());
        entity.setSku(request.getSku());
        entity.setEan(request.getEan());
        entity.setStock(request.getStock());
        entity.setCondition(request.getCondition());
        entity.setImages(request.getImages() == null ? new ArrayList<>() : request.getImages());
        entity.setSeller(request.getSeller());
        entity.setFreeShipping(request.getFreeShipping());
        entity.setWeightKg(request.getWeightKg());
        entity.setStatus(request.getStatus());
    }

    private ProductResponse toResponse(ProductEntity entity) {
        return ProductResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .category(entity.getCategory())
                .brand(entity.getBrand())
                .sku(entity.getSku())
                .ean(entity.getEan())
                .stock(entity.getStock())
                .condition(entity.getCondition())
                .images(entity.getImages())
                .seller(entity.getSeller())
                .freeShipping(entity.getFreeShipping())
                .weightKg(entity.getWeightKg())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
