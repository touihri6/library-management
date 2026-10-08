package com.example.loan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI loanOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Loan Service API")
                        .description("Loan Service - book loans CRUD")
                        .version("v1"));
    }
}
