package de.salonplan.customer;

import de.salonplan.appointment.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomerControllerTest extends PostgresIntegrationTest {

    private final MockMvc mvc;
    private final ObjectMapper objectMapper;

    @Autowired
    CustomerControllerTest(
            MockMvc mvc,
            ObjectMapper objectMapper
    ) {
        this.mvc = mvc;
        this.objectMapper = objectMapper;
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void createsCustomer() throws Exception {
        CustomerDtos.CustomerRequest request =
                new CustomerDtos.CustomerRequest(
                        "Lena",
                        "Test",
                        "lena.controller@example.test",
                        "01234"
                );

        byte[] requestBody =
                objectMapper.writeValueAsBytes(request);

        mvc.perform(
                        post("/api/customers")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(
                        jsonPath("$.firstName")
                                .value("Lena")
                )
                .andExpect(
                        jsonPath("$.lastName")
                                .value("Test")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(
                                        "lena.controller@example.test"
                                )
                );
    }

    @Test
    void anonymousUserReceivesUnauthorized() throws Exception {
        CustomerDtos.CustomerRequest request =
                new CustomerDtos.CustomerRequest(
                        "Lena",
                        "Test",
                        "anonymous@example.test",
                        "01234"
                );

        byte[] requestBody =
                objectMapper.writeValueAsBytes(request);

        mvc.perform(
                        post("/api/customers")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isUnauthorized());
    }
}