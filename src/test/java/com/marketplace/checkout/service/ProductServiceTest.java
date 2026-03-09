package com.marketplace.checkout.service;

import com.marketplace.checkout.domain.entities.ProductEntity;
import com.marketplace.checkout.domain.repository.ProductRepository;
import com.marketplace.checkout.dto.ProductRequest;
import com.marketplace.checkout.dto.ProductResponse;
import com.marketplace.checkout.exception.CheckoutException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("listProducts deve filtrar por search/category/status")
    void listProductsComFiltros() {
        when(productRepository.findAll()).thenReturn(List.of(
                buildEntity("p-1", "Notebook Gamer", "Eletronicos", "active", "NBG-001", "Marca A"),
                buildEntity("p-2", "Camiseta Azul", "Moda", "draft", "CAM-002", "Marca B")
        ));

        List<ProductResponse> result = productService.listProducts("notebook", "Eletronicos", "active");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("p-1");
    }

    @Test
    @DisplayName("createProduct deve persistir e retornar produto")
    void createProductComSucesso() {
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.createProduct(buildRequest("SKU-123"));

        assertThat(response.getId()).startsWith("p-");
        assertThat(response.getSku()).isEqualTo("SKU-123");
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("updateProduct deve atualizar dados quando produto existe")
    void updateProductComSucesso() {
        ProductEntity entity = buildEntity("p-1", "Antigo", "Eletronicos", "active", "SKU-OLD", "Marca A");
        when(productRepository.findById("p-1")).thenReturn(Optional.of(entity));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductRequest request = buildRequest("SKU-NEW");
        request.setTitle("Novo Titulo");
        ProductResponse response = productService.updateProduct("p-1", request);

        assertThat(response.getTitle()).isEqualTo("Novo Titulo");
        assertThat(response.getSku()).isEqualTo("SKU-NEW");
        assertThat(response.getUpdatedAt()).isAfterOrEqualTo(entity.getCreatedAt());
    }

    @Test
    @DisplayName("updateProduct deve lançar 404 quando produto não existe")
    void updateProductNaoEncontrado() {
        when(productRepository.findById("p-404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct("p-404", buildRequest("SKU-404")))
                .isInstanceOf(CheckoutException.class)
                .hasMessageContaining("Produto nao encontrado")
                .extracting("statusCode").isEqualTo(404);
    }

    @Test
    @DisplayName("deleteProduct deve remover quando existe")
    void deleteProductComSucesso() {
        when(productRepository.existsById("p-1")).thenReturn(true);

        productService.deleteProduct("p-1");

        verify(productRepository).deleteById("p-1");
    }

    @Test
    @DisplayName("deleteProduct deve lançar 404 quando não existe")
    void deleteProductNaoEncontrado() {
        when(productRepository.existsById("p-404")).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct("p-404"))
                .isInstanceOf(CheckoutException.class)
                .hasMessageContaining("Produto nao encontrado")
                .extracting("statusCode").isEqualTo(404);
    }

    @Test
    @DisplayName("createProduct deve copiar campos do request")
    void createProductDeveCopiarCampos() {
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.createProduct(buildRequest("SKU-CAPTURE"));

        ArgumentCaptor<ProductEntity> captor = ArgumentCaptor.forClass(ProductEntity.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getImages()).hasSize(1);
        assertThat(captor.getValue().getFreeShipping()).isTrue();
    }

    @Test
    @DisplayName("createProduct deve aceitar images nulo")
    void createProductComImagesNulo() {
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductRequest request = buildRequest("SKU-NO-IMAGE");
        request.setImages(null);

        ProductResponse response = productService.createProduct(request);

        assertThat(response.getImages()).isNotNull();
        assertThat(response.getImages()).isEmpty();
    }

    private ProductRequest buildRequest(String sku) {
        ProductRequest request = new ProductRequest();
        request.setTitle("Produto Teste");
        request.setDescription("Descricao");
        request.setPrice(new BigDecimal("99.90"));
        request.setCategory("Eletronicos");
        request.setBrand("Marca Teste");
        request.setSku(sku);
        request.setEan("7891234567890");
        request.setStock(10);
        request.setCondition("new");
        request.setImages(List.of("https://img.test/produto.jpg"));
        request.setSeller("Loja X");
        request.setFreeShipping(true);
        request.setWeightKg(new BigDecimal("1.250"));
        request.setStatus("active");
        return request;
    }

    private ProductEntity buildEntity(String id, String title, String category, String status, String sku, String brand) {
        ProductEntity entity = new ProductEntity();
        entity.setId(id);
        entity.setTitle(title);
        entity.setDescription("Descricao " + title);
        entity.setPrice(new BigDecimal("10.00"));
        entity.setCategory(category);
        entity.setBrand(brand);
        entity.setSku(sku);
        entity.setEan("7891231231231");
        entity.setStock(5);
        entity.setCondition("new");
        entity.setImages(List.of("https://img.test/" + id + ".jpg"));
        entity.setSeller("Loja");
        entity.setFreeShipping(false);
        entity.setWeightKg(new BigDecimal("0.900"));
        entity.setStatus(status);
        entity.setCreatedAt(OffsetDateTime.now().minusHours(1));
        entity.setUpdatedAt(OffsetDateTime.now().minusMinutes(10));
        return entity;
    }
}
