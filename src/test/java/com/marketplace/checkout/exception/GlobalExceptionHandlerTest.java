package com.marketplace.checkout.exception;

import com.marketplace.checkout.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("deve retornar resposta com status correto para CheckoutException")
    void deveRetornarRespostaParaCheckoutException() {
        CheckoutException ex = new CheckoutException("API Error", 422);

        var response = handler.handleCheckoutException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(422);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(422);
        assertThat(response.getBody().getError()).isEqualTo("Checkout Error");
        assertThat(response.getBody().getMessage()).isEqualTo("API Error");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("deve retornar 400 com lista de detalhes para MethodArgumentNotValidException")
    void deveRetornar400ParaValidationException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("obj", "items", "At least one item is required");
        FieldError fieldError2 = new FieldError("obj", "title", "Item title is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        var response = handler.handleValidation(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Validation Error");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid request data");
        assertThat(response.getBody().getDetails())
                .containsExactlyInAnyOrder("At least one item is required", "Item title is required");
    }

    @Test
    @DisplayName("deve retornar 500 para exceções genéricas")
    void deveRetornar500ParaExcecaoGenerica() {
        Exception ex = new RuntimeException("Unexpected failure");

        var response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
}
