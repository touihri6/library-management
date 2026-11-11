package com.example.book.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI bookOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Book Service API")
                        .description("Example CRUD API managing books: Controller -> Service -> Repository, with DTOs and mappers")
                        .version("v1")
                        .contact(new Contact().name("Library team").email("library@example.com"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("""
                                Keycloak access token of the `library` realm. Get one with:

                                `curl -d grant_type=password -d client_id=library-front -d username=librarian -d password=librarian http://localhost:8180/realms/library/protocol/openid-connect/token`

                                then paste the `access_token` value here (without the `Bearer ` prefix).""")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
