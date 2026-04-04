package de.salonplan.salonservice;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public final class SalonServiceDtos {
    private SalonServiceDtos() {}

    public record SalonServiceRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 500) String description,
            @Min(5) @Max(480) int durationMinutes,
            @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal price
    ) {}

    public record SalonServiceResponse(
            Long id,
            String name,
            String description,
            int durationMinutes,
            BigDecimal price,
            boolean active,
            Instant createdAt
    ) {}
}