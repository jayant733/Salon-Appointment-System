package de.salonplan.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class CustomerDtos {
    private CustomerDtos() {}

    public record CustomerRequest(
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 80) String lastName,
            @NotBlank @Email @Size(max = 150) String email,
            @Size(max = 40) String phone
    ) {}

    public record CustomerResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone,
            boolean active,
            Instant createdAt
    ) {}
}