package com.marketplace.checkout.dto.auth;

import com.marketplace.checkout.domain.entities.auth.AccountType;
import com.marketplace.checkout.domain.entities.auth.AuthProvider;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
public class AuthUserResponse {
    private String id;
    private String fullName;
    private String email;
    private String phoneMasked;
    private String documentMasked;
    private AccountType accountType;
    private LocalDate birthDate;
    private String companyName;
    private String tradeName;
    private String stateRegistration;
    private String zipCode;
    private String street;
    private String streetNumber;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;
    private AuthProvider authProvider;
    private boolean profileComplete;
    private OffsetDateTime createdAt;
}
