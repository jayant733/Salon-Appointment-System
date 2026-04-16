package de.salonplan.auth;

import de.salonplan.appointment.PostgresIntegrationTest;
import de.salonplan.user.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT extends PostgresIntegrationTest {

    private final MockMvc mvc;
    private final ObjectMapper objectMapper;
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    AuthControllerIT(
            MockMvc mvc,
            ObjectMapper objectMapper,
            AppUserRepository users,
            PasswordEncoder passwordEncoder
    ) {
        this.mvc = mvc;
        this.objectMapper = objectMapper;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Test
    void contextShouldLoad() {
    }
}