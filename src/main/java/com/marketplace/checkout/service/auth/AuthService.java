package com.marketplace.checkout.service.auth;

import com.marketplace.checkout.domain.entities.auth.AccountType;
import com.marketplace.checkout.domain.entities.auth.AuthProvider;
import com.marketplace.checkout.domain.entities.auth.UserEntity;
import com.marketplace.checkout.domain.repository.auth.UserRepository;
import com.marketplace.checkout.dto.auth.AuthLoginRequest;
import com.marketplace.checkout.dto.auth.AuthRegisterRequest;
import com.marketplace.checkout.dto.auth.AuthResponse;
import com.marketplace.checkout.dto.auth.AuthUserResponse;
import com.marketplace.checkout.dto.auth.GoogleAuthRequest;
import com.marketplace.checkout.exception.CheckoutException;
import com.marketplace.checkout.security.DataEncryptionService;
import com.marketplace.checkout.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataEncryptionService dataEncryptionService;
    private final JwtService jwtService;
    private final GoogleTokenService googleTokenService;

    public AuthResponse register(AuthRegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        userRepository.findByEmailIgnoreCase(normalizedEmail)
                .ifPresent(user -> {
                    throw new CheckoutException("Email already registered", 409);
                });

        validateMarketplaceProfile(request);

        OffsetDateTime now = OffsetDateTime.now();

        UserEntity user = new UserEntity();
        user.setId("u-" + UUID.randomUUID());
        user.setFullName(request.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setGoogleSubject(null);
        user.setAccountType(request.getAccountType());
        user.setDocumentEncrypted(dataEncryptionService.encrypt(onlyDigits(request.getCpfCnpj())));
        user.setPhoneEncrypted(dataEncryptionService.encrypt(onlyDigits(request.getPhone())));
        user.setBirthDate(request.getBirthDate());
        user.setCompanyName(trimToNull(request.getCompanyName()));
        user.setTradeName(trimToNull(request.getTradeName()));
        user.setStateRegistration(trimToNull(request.getStateRegistration()));
        user.setZipCode(onlyDigits(request.getZipCode()));
        user.setStreet(request.getStreet().trim());
        user.setStreetNumber(request.getStreetNumber().trim());
        user.setComplement(trimToNull(request.getComplement()));
        user.setNeighborhood(request.getNeighborhood().trim());
        user.setCity(request.getCity().trim());
        user.setState(request.getState().trim().toUpperCase());
        user.setAcceptedTermsAt(now);
        user.setProfileComplete(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(AuthLoginRequest request) {
        UserEntity user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new CheckoutException("Invalid credentials", 401));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new CheckoutException("Invalid credentials", 401);
        }

        return buildAuthResponse(user);
    }

    public AuthResponse loginWithGoogle(GoogleAuthRequest request) {
        GoogleTokenService.GooglePayload payload = googleTokenService.verify(request.getIdToken());
        String normalizedEmail = normalizeEmail(payload.email());

        Optional<UserEntity> existing = userRepository.findByEmailIgnoreCase(normalizedEmail);
        UserEntity user = existing.orElseGet(() -> createGoogleUser(payload, normalizedEmail));

        if (user.getGoogleSubject() == null) {
            user.setGoogleSubject(payload.subject());
            user.setAuthProvider(AuthProvider.GOOGLE);
            user.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(user);
        }

        return buildAuthResponse(user);
    }

    public AuthUserResponse getMe(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CheckoutException("User not found", 404));
        return toResponse(user);
    }

    private UserEntity createGoogleUser(GoogleTokenService.GooglePayload payload, String normalizedEmail) {
        OffsetDateTime now = OffsetDateTime.now();
        UserEntity user = new UserEntity();
        user.setId("u-" + UUID.randomUUID());
        user.setFullName(payload.name() != null ? payload.name() : "Usuário Google");
        user.setEmail(normalizedEmail);
        user.setPasswordHash(null);
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setGoogleSubject(payload.subject());
        user.setAccountType(AccountType.INDIVIDUAL);
        user.setDocumentEncrypted(dataEncryptionService.encrypt("00000000000"));
        user.setPhoneEncrypted(dataEncryptionService.encrypt("00000000000"));
        user.setBirthDate(null);
        user.setCompanyName(null);
        user.setTradeName(null);
        user.setStateRegistration(null);
        user.setZipCode("");
        user.setStreet("");
        user.setStreetNumber("");
        user.setComplement(null);
        user.setNeighborhood("");
        user.setCity("");
        user.setState("SP");
        user.setAcceptedTermsAt(now);
        user.setProfileComplete(false);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(UserEntity user) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateToken(user))
                .tokenType("Bearer")
                .expiresAt(jwtService.getExpirationDateTime())
                .user(toResponse(user))
                .build();
    }

    private AuthUserResponse toResponse(UserEntity user) {
        String phone = dataEncryptionService.decrypt(user.getPhoneEncrypted());
        String document = dataEncryptionService.decrypt(user.getDocumentEncrypted());

        return AuthUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneMasked(maskPhone(phone))
                .documentMasked(maskDocument(document))
                .accountType(user.getAccountType())
                .birthDate(user.getBirthDate())
                .companyName(user.getCompanyName())
                .tradeName(user.getTradeName())
                .stateRegistration(user.getStateRegistration())
                .zipCode(user.getZipCode())
                .street(user.getStreet())
                .streetNumber(user.getStreetNumber())
                .complement(user.getComplement())
                .neighborhood(user.getNeighborhood())
                .city(user.getCity())
                .state(user.getState())
                .authProvider(user.getAuthProvider())
                .profileComplete(Boolean.TRUE.equals(user.getProfileComplete()))
                .createdAt(user.getCreatedAt())
                .build();
    }

    private void validateMarketplaceProfile(AuthRegisterRequest request) {
        String document = onlyDigits(request.getCpfCnpj());
        String phone = onlyDigits(request.getPhone());
        String zipCode = onlyDigits(request.getZipCode());

        if (document.length() != 11 && document.length() != 14) {
            throw new CheckoutException("CPF/CNPJ must have 11 or 14 digits", 400);
        }
        if (phone.length() < 10 || phone.length() > 11) {
            throw new CheckoutException("Phone must have 10 or 11 digits", 400);
        }
        if (zipCode.length() != 8) {
            throw new CheckoutException("Zip code must have 8 digits", 400);
        }

        if (request.getAccountType() == AccountType.INDIVIDUAL && request.getBirthDate() == null) {
            throw new CheckoutException("Birth date is required for individual accounts", 400);
        }
        if (request.getAccountType() == AccountType.INDIVIDUAL && document.length() != 11) {
            throw new CheckoutException("Individual account must use CPF", 400);
        }

        if (request.getAccountType() == AccountType.BUSINESS) {
            if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
                throw new CheckoutException("Company name is required for business accounts", 400);
            }
            if (document.length() != 14) {
                throw new CheckoutException("Business account must use CNPJ", 400);
            }
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String onlyDigits(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\D", "");
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String maskDocument(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        if (value.length() <= 4) {
            return "****";
        }

        return "*".repeat(Math.max(0, value.length() - 4)) + value.substring(value.length() - 4);
    }

    private String maskPhone(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        if (value.length() <= 4) {
            return "****";
        }

        return "*".repeat(Math.max(0, value.length() - 4)) + value.substring(value.length() - 4);
    }
}
