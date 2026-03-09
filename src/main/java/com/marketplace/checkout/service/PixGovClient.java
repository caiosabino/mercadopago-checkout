package com.marketplace.checkout.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.marketplace.checkout.config.PixGovProperties;
import com.marketplace.checkout.dto.PixPaymentRequest;
import com.marketplace.checkout.exception.CheckoutException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PixGovClient {

    private final RestClient.Builder restClientBuilder;
    private final PixGovProperties properties;

    public PixChargeData createImmediateCharge(PixPaymentRequest request) {
        if (Boolean.TRUE.equals(properties.getMockEnabled())) {
            return buildMockCharge(request);
        }

        validateConfiguration();

        String txId = (request.getTxId() != null && !request.getTxId().isBlank())
                ? request.getTxId()
                : UUID.randomUUID().toString().replace("-", "").substring(0, 32);

        RestClient pixClient = restClientBuilder.baseUrl(properties.getBaseUrl()).build();
        String accessToken = getAccessToken();

        CobRequest cobRequest = buildCobRequest(request);
        CobResponse cobResponse = pixClient.put()
                .uri("/v2/cob/{txid}", txId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(cobRequest)
                .retrieve()
                .body(CobResponse.class);

        if (cobResponse == null || cobResponse.getLoc() == null || cobResponse.getLoc().getId() == null) {
            throw new CheckoutException("Pix API returned invalid charge payload", 500);
        }

        QrCodeResponse qrCodeResponse = pixClient.get()
                .uri("/v2/loc/{id}/qrcode", cobResponse.getLoc().getId())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(QrCodeResponse.class);

        return PixChargeData.builder()
                .txId(cobResponse.getTxid())
                .status(cobResponse.getStatus())
                .qrCode(qrCodeResponse != null ? qrCodeResponse.getQrcode() : null)
                .qrCodeBase64(qrCodeResponse != null ? qrCodeResponse.getImagemQrcode() : null)
                .location(cobResponse.getLoc().getLocation())
                .createdAt(cobResponse.getCalendario() != null ? cobResponse.getCalendario().getCriacao() : null)
                .build();
    }

    private CobRequest buildCobRequest(PixPaymentRequest request) {
        CobRequest payload = new CobRequest();

        CalendarioRequest calendario = new CalendarioRequest();
        calendario.setExpiracao(properties.getDefaultExpirationSeconds());
        payload.setCalendario(calendario);

        ValorRequest valor = new ValorRequest();
        valor.setOriginal(request.getTransactionAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
        payload.setValor(valor);

        payload.setChave(properties.getReceiverKey());
        payload.setSolicitacaoPagador(request.getDescription());

        if (request.getPayer() != null && request.getPayer().getName() != null && !request.getPayer().getName().isBlank()) {
            DevedorRequest devedor = new DevedorRequest();
            devedor.setNome(request.getPayer().getName());
            devedor.setCpf(request.getPayer().getCpf());
            devedor.setCnpj(request.getPayer().getCnpj());
            payload.setDevedor(devedor);
        }

        return payload;
    }

    private String getAccessToken() {
        if (properties.getBearerToken() != null && !properties.getBearerToken().isBlank()) {
            return properties.getBearerToken();
        }

        if (properties.getOauthTokenUrl() == null || properties.getOauthTokenUrl().isBlank()) {
            throw new CheckoutException("Configure pix.gov.bearer-token or OAuth credentials for Pix API", 500);
        }

        RestClient authClient = restClientBuilder.build();
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("scope", properties.getScope());

        TokenResponse token = authClient.post()
                .uri(properties.getOauthTokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(h -> h.setBasicAuth(properties.getClientId(), properties.getClientSecret()))
                .body(toFormUrlEncoded(form))
                .retrieve()
                .body(TokenResponse.class);

        if (token == null || token.getAccessToken() == null || token.getAccessToken().isBlank()) {
            throw new CheckoutException("Could not retrieve OAuth token for Pix API", 500);
        }

        return token.getAccessToken();
    }

    private String toFormUrlEncoded(MultiValueMap<String, String> data) {
        return data.entrySet().stream()
                .flatMap(e -> e.getValue().stream().map(v -> encode(e.getKey()) + "=" + encode(v)))
                .reduce((a, b) -> a + "&" + b)
                .orElse("");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void validateConfiguration() {
        if (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
            throw new CheckoutException("Configure pix.gov.base-url", 500);
        }
        if (properties.getReceiverKey() == null || properties.getReceiverKey().isBlank()) {
            throw new CheckoutException("Configure pix.gov.receiver-key", 500);
        }
    }

    private PixChargeData buildMockCharge(PixPaymentRequest request) {
        String txId = (request.getTxId() != null && !request.getTxId().isBlank())
                ? request.getTxId()
                : "MOCK" + UUID.randomUUID().toString().replace("-", "").substring(0, 28);

        String qrCode = "0002010102122688pix.mock.gov/charge/" + txId + "5204000053039865802BR5909CHECKOUT6009SAOPAULO62070503***6304ABCD";
        String qrBase64 = Base64.getEncoder().encodeToString(("QR:" + txId).getBytes(StandardCharsets.UTF_8));

        return PixChargeData.builder()
                .txId(txId)
                .status("ATIVA")
                .qrCode(qrCode)
                .qrCodeBase64(qrBase64)
                .location("https://pix.mock.gov/loc/" + txId)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Data
    private static class CobRequest {
        private CalendarioRequest calendario;
        private DevedorRequest devedor;
        private ValorRequest valor;
        private String chave;
        private String solicitacaoPagador;
    }

    @Data
    private static class CalendarioRequest {
        private Integer expiracao;
    }

    @Data
    private static class DevedorRequest {
        private String cpf;
        private String cnpj;
        private String nome;
    }

    @Data
    private static class ValorRequest {
        private String original;
    }

    @Data
    private static class CobResponse {
        private String txid;
        private String status;
        private CalendarioResponse calendario;
        private LocResponse loc;
    }

    @Data
    private static class CalendarioResponse {
        private OffsetDateTime criacao;
    }

    @Data
    private static class LocResponse {
        private Integer id;
        private String location;
    }

    @Data
    private static class QrCodeResponse {
        private String qrcode;
        private String imagemQrcode;
    }

    @Data
    private static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
    }
}
