package de.salonplan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "salonplan.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        long accessTokenMinutes
) {
}