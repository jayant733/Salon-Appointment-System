package de.salonplan.auth;

import de.salonplan.config.JwtProperties;
import de.salonplan.user.AppUser;
import de.salonplan.user.AppUserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public AuthService(
            AppUserRepository users,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            JwtProperties jwtProperties,
            Clock clock
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    public AuthDtos.LoginResponse login(
            AuthDtos.LoginRequest request
    ) {
        String normalizedEmail = normalizeEmail(request.email());

        AppUser user = users
                .findByEmailIgnoreCase(normalizedEmail)
                .filter(AppUser::isEnabled)
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw invalidCredentials();
        }

        Instant issuedAt = clock.instant();

        Instant expiresAt = issuedAt.plus(
                jwtProperties.accessTokenMinutes(),
                ChronoUnit.MINUTES
        );

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(jwtProperties.issuer())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .claim(
                        "roles",
                        List.of(user.getRole().name())
                )
                .claim(
                        "userId",
                        user.getId()
                )
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        String accessToken = jwtEncoder
                .encode(
                        JwtEncoderParameters.from(
                                header,
                                claims
                        )
                )
                .getTokenValue();

        return new AuthDtos.LoginResponse(
                accessToken,
                "Bearer",
                expiresAt,
                user.getEmail(),
                user.getRole()
        );
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw invalidCredentials();
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private BadCredentialsException invalidCredentials() {
        return new BadCredentialsException(
                "Invalid credentials"
        );
    }
}