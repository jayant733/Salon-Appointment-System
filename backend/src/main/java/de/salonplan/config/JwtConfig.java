package de.salonplan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    private static final int MINIMUM_SECRET_LENGTH_BYTES = 32;

    @Bean
    public SecretKey jwtSecretKey(JwtProperties properties) {
        String configuredSecret = properties.secret();

        if (configuredSecret == null || configuredSecret.isBlank()) {
            throw new IllegalStateException(
                    "Die Eigenschaft salonplan.jwt.secret darf nicht leer sein."
            );
        }

        byte[] secretBytes =
                configuredSecret.getBytes(StandardCharsets.UTF_8);

        if (secretBytes.length < MINIMUM_SECRET_LENGTH_BYTES) {
            throw new IllegalStateException(
                    "Die Eigenschaft salonplan.jwt.secret muss für HS256 "
                            + "mindestens 32 Byte lang sein. Aktuelle Länge: "
                            + secretBytes.length + " Byte."
            );
        }

        return new SecretKeySpec(
                secretBytes,
                "HmacSHA256"
        );
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return NimbusJwtEncoder
                .withSecretKey(jwtSecretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}