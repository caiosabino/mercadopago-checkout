package com.marketplace.checkout.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private OffsetDateTime expiresAt;
    private AuthUserResponse user;
}
