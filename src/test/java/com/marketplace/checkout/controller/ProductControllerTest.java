package com.marketplace.checkout.controller;

import com.marketplace.checkout.dto.ProductResponse;
import com.marketplace.checkout.exception.CheckoutException;
import com.marketplace.checkout.exception.GlobalExceptionHandler;
import com.marketplace.checkout.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController")
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /products deve listar produtos")
    void deveListarProdutos() throws Exception {
        when(productService.listProducts("note", "Eletronicos", "active"))
                .thenReturn(List.of(buildResponse("p-1", "Notebook")));

        mockMvc.perform(get("/products")
                        .param("search", "note")
                        .param("category", "Eletronicos")
                        .param("status", "active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("p-1"))
                .andExpect(jsonPath("$[0].title").value("Notebook"));
    }

    @Test
    @DisplayName("POST /products deve criar produto")
    void deveCriarProduto() throws Exception {
        when(productService.createProduct(any())).thenReturn(buildResponse("p-10", "Produto Novo"));

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("p-10"))
                .andExpect(jsonPath("$.title").value("Produto Novo"));
    }

    @Test
    @DisplayName("POST /products deve criar produto sem images")
    void deveCriarProdutoSemImages() throws Exception {
        when(productService.createProduct(any())).thenReturn(buildResponse("p-11", "Produto Sem Imagem"));

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayloadSemImages()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("p-11"));
    }

    @Test
    @DisplayName("POST /products deve validar payload")
    void deveValidarPayloadNoCreate() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));

        verify(productService, never()).createProduct(any());
    }

    @Test
    @DisplayName("PUT /products/{id} deve atualizar produto")
    void deveAtualizarProduto() throws Exception {
        when(productService.updateProduct(any(), any())).thenReturn(buildResponse("p-1", "Produto Atualizado"));

        mockMvc.perform(put("/products/p-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Produto Atualizado"));
    }

    @Test
    @DisplayName("PUT /products/{id} deve retornar 404 quando não encontrado")
    void deveRetornar404NoUpdate() throws Exception {
        when(productService.updateProduct(any(), any()))
                .thenThrow(new CheckoutException("Produto nao encontrado", 404));

        mockMvc.perform(put("/products/p-404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Produto nao encontrado"));
    }

    @Test
    @DisplayName("DELETE /products/{id} deve retornar 204")
    void deveRemoverProduto() throws Exception {
        mockMvc.perform(delete("/products/p-1"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct("p-1");
    }

    private ProductResponse buildResponse(String id, String title) {
        return ProductResponse.builder()
                .id(id)
                .title(title)
                .description("Descricao")
                .price(new BigDecimal("100.00"))
                .category("Eletronicos")
                .brand("Marca")
                .sku("SKU-1")
                .ean("7891234567890")
                .stock(10)
                .condition("new")
                .images(List.of("https://img.test/produto.jpg"))
                .seller("Loja X")
                .freeShipping(true)
                .weightKg(new BigDecimal("1.000"))
                .status("active")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    private String validPayload() {
        return """
                {
                  "title": "Produto Teste",
                  "description": "Descricao produto",
                  "price": 99.90,
                  "category": "Eletronicos",
                  "brand": "Marca X",
                  "sku": "SKU-123",
                  "ean": "7891234567890",
                  "stock": 12,
                  "condition": "new",
                  "images": ["https://img.test/produto.jpg"],
                  "seller": "Loja X",
                  "freeShipping": true,
                  "weightKg": 1.250,
                  "status": "active"
                }
                """;
    }

    private String validPayloadSemImages() {
        return """
                {
                  "title": "Produto Teste",
                  "description": "Descricao produto",
                  "price": 99.90,
                  "category": "Eletronicos",
                  "brand": "Marca X",
                  "sku": "SKU-123",
                  "ean": "7891234567890",
                  "stock": 12,
                  "condition": "new",
                  "seller": "Loja X",
                  "freeShipping": true,
                  "weightKg": 1.250,
                  "status": "active"
                }
                """;
    }
}
