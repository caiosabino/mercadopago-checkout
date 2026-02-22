package com.marketplace.checkout.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CheckoutException")
class CheckoutExceptionTest {

    @Test
    @DisplayName("deve criar exceção com mensagem e statusCode")
    void deveCriarExcecaoComMensagemEStatus() {
        CheckoutException ex = new CheckoutException("Erro de pagamento", 402);

        assertThat(ex.getMessage()).isEqualTo("Erro de pagamento");
        assertThat(ex.getStatusCode()).isEqualTo(402);
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("deve criar exceção com mensagem, statusCode e causa")
    void deveCriarExcecaoComCausa() {
        RuntimeException cause = new RuntimeException("causa original");
        CheckoutException ex = new CheckoutException("Erro encapsulado", 500, cause);

        assertThat(ex.getMessage()).isEqualTo("Erro encapsulado");
        assertThat(ex.getStatusCode()).isEqualTo(500);
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("deve ser RuntimeException")
    void deveSerRuntimeException() {
        CheckoutException ex = new CheckoutException("test", 400);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
