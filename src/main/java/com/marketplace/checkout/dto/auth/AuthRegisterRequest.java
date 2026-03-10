package com.marketplace.checkout.dto.auth;

import com.marketplace.checkout.domain.entities.auth.AccountType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AuthRegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "CPF/CNPJ is required")
    private String cpfCnpj;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    private LocalDate birthDate;
    private String companyName;
    private String tradeName;
    private String stateRegistration;

    @NotBlank(message = "Zip code is required")
    private String zipCode;

    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "Street number is required")
    private String streetNumber;

    private String complement;

    @NotBlank(message = "Neighborhood is required")
    private String neighborhood;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    @Pattern(regexp = "^[A-Za-z]{2}$", message = "State must have 2 letters")
    private String state;

    private boolean acceptedTerms;

    @AssertTrue(message = "Terms must be accepted")
    public boolean isAcceptedTerms() {
        return acceptedTerms;
    }
}
