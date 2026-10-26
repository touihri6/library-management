package com.example.event.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    private static RequestPostProcessor as(String... roles) {
        return jwt().jwt(token -> token.claim("preferred_username", "user")
                        .claim("realm_access", Map.of("roles", List.of(roles))))
                .authorities(new KeycloakRealmRoleConverter());
    }

    @Test
    void swaggerIsPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousCanReadTheCatalog() throws Exception {
        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/events/1"))
                .andExpect(status().isOk());
    }

    @Test
    void writeWithoutTokenIsRejectedWith401() throws Exception {
        mockMvc.perform(post("/api/v1/events").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", startsWith("Bearer")));
    }

    @Test
    void memberCannotChangeTheCatalog() throws Exception {
        mockMvc.perform(post("/api/v1/events").contentType(MediaType.APPLICATION_JSON).content("{}").with(as("MEMBER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/events/1").contentType(MediaType.APPLICATION_JSON).content("{}").with(as("MEMBER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/events/1").with(as("MEMBER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void librarianPassesSecurityForWrites() throws Exception {
        mockMvc.perform(post("/api/v1/events").contentType(MediaType.APPLICATION_JSON).content("{}").with(as("LIBRARIAN")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(delete("/api/v1/events/1").with(as("LIBRARIAN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminCanChangeTheCatalog() throws Exception {
        mockMvc.perform(delete("/api/v1/events/1").with(as("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void userWithoutRoleCannotChangeTheCatalog() throws Exception {
        mockMvc.perform(delete("/api/v1/events/1").with(as()))
                .andExpect(status().isForbidden());
    }
}
