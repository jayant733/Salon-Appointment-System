package de.salonplan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("salonplan.bootstrap")
public record BootstrapProperties(
        String adminEmail,
        String adminPassword,
        String employeeEmail,
        String employeePassword
) {
}