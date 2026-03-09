package com.marketplace.checkout.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "pix.gov")
public class PixGovProperties {
    private Boolean mockEnabled = true;
    private String baseUrl;
    private String receiverKey;
    private Integer defaultExpirationSeconds = 3600;
    private String bearerToken;
    private String oauthTokenUrl;
    private String clientId;
    private String clientSecret;
    private String scope = "cob.write cob.read payloadlocation.read";
}
