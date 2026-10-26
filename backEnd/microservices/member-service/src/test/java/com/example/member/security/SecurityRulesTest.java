package com.example.member.security;

import com.example.member.client.LoanClient;
import com.example.member.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanClient loanClient;

    @BeforeEach
    void setUp() {
        when(loanClient.findByMember(anyLong(), anyInt())).thenReturn(new PageResponse<>(List.of(), 0, 100, 0, 0, true));
    }

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
    void readWithoutTokenIsRejectedWith401() throws Exception {
        mockMvc.perform(get("/api/v1/members"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", startsWith("Bearer")));
    }

    @Test
    void memberIsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/members").with(as("MEMBER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/members/1").with(as("MEMBER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void librarianAndAdminCanRead() throws Exception {
        mockMvc.perform(get("/api/v1/members").with(as("LIBRARIAN")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/members/1").with(as("ADMIN")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/members/1/loans").with(as("LIBRARIAN")))
                .andExpect(status().isOk());
    }

    @Test
    void librarianCanDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/members/1").with(as("LIBRARIAN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void serviceAccountCanOnlySearchAndCreateMembers() throws Exception {
        mockMvc.perform(get("/api/v1/members").param("email", "amine@mail.com").with(as("SERVICE")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/members").contentType(MediaType.APPLICATION_JSON).content("{}").with(as("SERVICE")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/members/1").with(as("SERVICE")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/members/1").with(as("SERVICE")))
                .andExpect(status().isForbidden());
    }
}
