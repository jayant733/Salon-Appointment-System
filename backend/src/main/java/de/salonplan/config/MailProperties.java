package de.salonplan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("salonplan.mail")
public record MailProperties(boolean enabled, String from) {
}