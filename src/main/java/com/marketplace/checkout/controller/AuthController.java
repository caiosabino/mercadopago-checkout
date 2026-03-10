package com.marketplace.checkout.controller;

import com.marketplace.checkout.dto.auth.AuthLoginRequest;
import com.marketplace.checkout.dto.auth.AuthRegisterRequest;
import com.marketplace.checkout.dto.auth.AuthResponse;
import com.marketplace.checkout.dto.auth.AuthUserResponse;
import com.marketplace.checkout.dto.auth.GoogleAuthRequest;
import com.marketplace.checkout.exception.CheckoutException;
import com.marketplace.checkout.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleAuthRequest request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserResponse> me(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new CheckoutException("Unauthorized", 401);
        }

        String userId = authentication.getPrincipal().toString();
        return ResponseEntity.ok(authService.getMe(userId));
    }
}
