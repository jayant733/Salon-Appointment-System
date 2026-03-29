package de.salonplan.auth;

import de.salonplan.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {
    }

    public record LoginResponse(
            String accessToken,
            String tokenType,
            Instant expiresAt,
            String email,
            UserRole role
    ) {
    }
}