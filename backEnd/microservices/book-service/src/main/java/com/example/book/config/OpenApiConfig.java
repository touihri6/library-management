package com.example.book.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Book Service API")
                        .description("Example CRUD API managing books: Controller -> Service -> Repository, with DTOs and mappers")
                        .version("v1")
                        .contact(new Contact().name("Library team").email("library@example.com"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
