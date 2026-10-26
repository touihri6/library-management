package com.example.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .description("User Service - library users backed by Keycloak")
                        .version("v1"));
    }
}
