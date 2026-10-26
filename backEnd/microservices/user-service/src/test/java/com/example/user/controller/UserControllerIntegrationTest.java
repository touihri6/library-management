package com.example.user.controller;

import com.example.user.client.LoanClient;
import com.example.user.client.MemberClient;
import com.example.user.dto.LoanSummary;
import com.example.user.dto.MemberSummary;
import com.example.user.dto.PageResponse;
import com.example.user.keycloak.KeycloakAdminClient;
import com.example.user.security.KeycloakRealmRoleConverter;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    private static final String REGISTER = """
            {
              "username": "amine",
              "email": "amine@mail.com",
              "firstName": "Amine",
              "lastName": "Ben Ali",
              "password": "S3cret!pass"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KeycloakAdminClient keycloakAdminClient;

    @MockitoBean
    private MemberClient memberClient;

    @MockitoBean
    private LoanClient loanClient;

    @BeforeEach
    void setUp() {
        when(memberClient.findByEmail(anyString(), anyInt())).thenReturn(new PageResponse<>(List.of(), 0, 1, 0, 0, true));
    }

    @Test
    void register_alsoCreatesTheLibraryMember() throws Exception {
        when(keycloakAdminClient.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("44444444-4444-4444-4444-444444444444");

        mockMvc.perform(post("/api/v1/users/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER))
                .andExpect(status().isCreated());
        verify(memberClient).create(any());
    }

    @Test
    void myLoans_returnsTheLinkedMemberAndTheirLoans() throws Exception {
        when(memberClient.findByEmail("member@library.tn", 1)).thenReturn(new PageResponse<>(List.of(
                new MemberSummary(7L, "Youssef", "Member", "member@library.tn", "STANDARD")), 0, 1, 1, 1, true));
        when(loanClient.findByMember(7L, 100)).thenReturn(new PageResponse<>(List.of(
                new LoanSummary(1L, 3L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15), null, "ONGOING")),
                0, 100, 1, 1, true));

        mockMvc.perform(get("/api/v1/users/me/loans").with(jwt().jwt(token -> token
                        .subject("33333333-3333-3333-3333-333333333333")
                        .claim("email", "member@library.tn")
                        .claim("realm_access", Map.of("roles", List.of("MEMBER"))))
                        .authorities(new KeycloakRealmRoleConverter())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.member.id").value(7))
                .andExpect(jsonPath("$.loans[0].status").value("ONGOING"));
    }

    @Test
    void myLoans_withoutLinkedMember_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/loans").with(jwt().jwt(token -> token
                        .claim("email", "nobody@mail.com")
                        .claim("realm_access", Map.of("roles", List.of("MEMBER"))))
                        .authorities(new KeycloakRealmRoleConverter())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No library member is linked to nobody@mail.com"));
    }

    @Test
    void myLoans_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/loans"))
                .andExpect(status().isUnauthorized());
    }

    private static RequestPostProcessor as(String username, String subject, String... roles) {
        return jwt().jwt(token -> token.subject(subject)
                        .claim("preferred_username", username)
                        .claim("realm_access", Map.of("roles", List.of(roles))))
                .authorities(new KeycloakRealmRoleConverter());
    }

    private static RequestPostProcessor admin() {
        return as("admin", "11111111-1111-1111-1111-111111111111", "ADMIN");
    }

    @Test
    void register_isPublicAndCreatesMember() throws Exception {
        when(keycloakAdminClient.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("44444444-4444-4444-4444-444444444444");

        mockMvc.perform(post("/api/v1/users/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("http://localhost/api/v1/users/")))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.keycloakId").value("44444444-4444-4444-4444-444444444444"));
        verify(keycloakAdminClient).assignRealmRole("44444444-4444-4444-4444-444444444444", "MEMBER");
    }

    @Test
    void register_withInvalidPayload_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER.replace("S3cret!pass", "short")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
        verifyNoInteractions(keycloakAdminClient);
    }

    @Test
    void register_withExistingUsername_returns409() throws Exception {
        mockMvc.perform(post("/api/v1/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER.replace("\"amine\"", "\"member\"")))
                .andExpect(status().isConflict());
    }

    @Test
    void me_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_returnsProfileOfTokenSubject() throws Exception {
        mockMvc.perform(get("/api/v1/users/me").with(as("member", "33333333-3333-3333-3333-333333333333", "MEMBER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("member"))
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    @Test
    void list_asMember_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/users").with(as("member", "33333333-3333-3333-3333-333333333333", "MEMBER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_asAdmin_returnsAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void list_filteredByRole() throws Exception {
        mockMvc.perform(get("/api/v1/users").param("role", "LIBRARIAN").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].username").value("librarian"));
    }

    @Test
    void create_asLibrarian_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .with(as("librarian", "22222222-2222-2222-2222-222222222222", "LIBRARIAN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void changeRole_asAdmin_returnsNewRole() throws Exception {
        mockMvc.perform(put("/api/v1/users/3/role").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"LIBRARIAN\"}").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("LIBRARIAN"));
        verify(keycloakAdminClient).assignRealmRole("33333333-3333-3333-3333-333333333333", "LIBRARIAN");
    }

    @Test
    void disable_asAdmin_returnsDisabled() throws Exception {
        mockMvc.perform(patch("/api/v1/users/3/disable").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void delete_asAdmin_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/users/3").with(admin()))
                .andExpect(status().isNoContent());
        verify(keycloakAdminClient).deleteUser("33333333-3333-3333-3333-333333333333");
    }

    @Test
    void getUnknown_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/users/999").with(admin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void swaggerIsPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}
