package com.marketplace.checkout.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.marketplace.checkout.exception.CheckoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleTokenService {

    private final String googleClientId;
    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenService(@Value("${security.google.client-id:}") String googleClientId) {
        this.googleClientId = googleClientId;
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    public GooglePayload verify(String idToken) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new CheckoutException("Google login is not configured. Set GOOGLE_CLIENT_ID", 500);
        }

        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new CheckoutException("Invalid Google ID token", 401);
            }

            GoogleIdToken.Payload payload = token.getPayload();
            boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
            if (!emailVerified) {
                throw new CheckoutException("Google account email is not verified", 401);
            }

            return new GooglePayload(
                    payload.getSubject(),
                    payload.getEmail(),
                    (String) payload.get("name")
            );
        } catch (GeneralSecurityException | IOException ex) {
            throw new CheckoutException("Failed to validate Google token", 401, ex);
        }
    }

    public record GooglePayload(String subject, String email, String name) {
    }
}
