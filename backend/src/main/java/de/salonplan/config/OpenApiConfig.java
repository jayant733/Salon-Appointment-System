package de.salonplan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI salonPlanOpenApi() {
        return new OpenAPI().info(new Info()
                .title("SalonPlan API")
                .version("1.0")
                .description("REST API für Kunden, Mitarbeiter, Leistungen und Termine"));
    }
}