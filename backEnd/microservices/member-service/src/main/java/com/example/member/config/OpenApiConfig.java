package com.example.member.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI memberOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Member Service API")
                        .description("Member Service - library members CRUD")
                        .version("v1"));
    }
}
