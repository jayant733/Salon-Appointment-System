package de.salonplan.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class EmployeeDtos {
    private EmployeeDtos() {}

    public record EmployeeRequest(
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 80) String lastName,
            @NotBlank @Email @Size(max = 150) String email
    ) {}

    public record EmployeeResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            boolean active,
            Long userId,
            Instant createdAt
    ) {}
}