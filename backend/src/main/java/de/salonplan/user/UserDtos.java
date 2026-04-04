package de.salonplan.user;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class UserDtos {
    private UserDtos() {}

    public record UserResponse(
            Long id,
            String email,
            UserRole role,
            boolean enabled,
            Instant createdAt
    ) {}

    public record ChangeRoleRequest(@NotNull UserRole role) {}

    public record ChangeEnabledRequest(@NotNull Boolean enabled) {}
}