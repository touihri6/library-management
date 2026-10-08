package com.example.event.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Event Service API")
                        .description("Event Service - library events CRUD")
                        .version("v1"));
    }
}
