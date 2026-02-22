package com.marketplace.checkout.controller;

import com.marketplace.checkout.dto.*;
import com.marketplace.checkout.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Mercado Pago Checkout Preferences API")
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/preferences")
    @Operation(summary = "Create checkout preference", description = "Creates a payment preference on Mercado Pago and returns the checkout URL")
    public ResponseEntity<CheckoutPreferenceResponse> createPreference(
            @Valid @RequestBody CheckoutPreferenceRequest request) {

        log.info("POST /api/checkout/preferences - Creating preference");
        CheckoutPreferenceResponse response = checkoutService.createPreference(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/preferences/{id}")
    @Operation(summary = "Get preference by ID")
    public ResponseEntity<CheckoutPreferenceResponse> getPreference(@PathVariable String id) {
        log.info("GET /api/checkout/preferences/{}", id);
        return ResponseEntity.ok(checkoutService.getPreference(id));
    }
}
