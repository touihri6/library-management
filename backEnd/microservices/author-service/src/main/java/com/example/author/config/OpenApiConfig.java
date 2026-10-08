package com.example.author.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authorOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Author Service API")
                        .description("Author Service - authors CRUD")
                        .version("v1"));
    }
}
