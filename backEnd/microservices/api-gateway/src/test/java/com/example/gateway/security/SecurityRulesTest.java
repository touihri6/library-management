package com.example.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
@AutoConfigureMockMvc
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    private static RequestPostProcessor as(String... roles) {
        return jwt().jwt(token -> token.claim("preferred_username", "user")
                        .claim("realm_access", Map.of("roles", List.of(roles))))
                .authorities(new KeycloakRealmRoleConverter());
    }

    private void expectRouted(MockHttpServletRequestBuilder request) {
        assertThatThrownBy(() -> mockMvc.perform(request))
                .hasRootCauseInstanceOf(HttpServerErrorException.class)
                .rootCause()
                .hasMessageContaining("Unable to find instance");
    }

    @Test
    void protectedRouteWithoutTokenIsRejectedWith401() throws Exception {
        mockMvc.perform(get("/api/v1/loans"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", startsWith("Bearer")));
    }

    @Test
    void catalogReadsArePublicAndReachTheRoute() {
        expectRouted(get("/api/v1/books"));
        expectRouted(get("/api/v1/authors/1"));
        expectRouted(get("/api/v1/events"));
        expectRouted(get("/api/v1/reviews"));
    }

    @Test
    void registrationIsPublic() {
        expectRouted(post("/api/v1/users/register").contentType(MediaType.APPLICATION_JSON).content("{}"));
    }

    @Test
    void memberCannotChangeTheCatalog() throws Exception {
        mockMvc.perform(post("/api/v1/books").contentType(MediaType.APPLICATION_JSON).content("{}").with(as("MEMBER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/authors/1").with(as("MEMBER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void librarianCanChangeTheCatalog() {
        expectRouted(post("/api/v1/books").contentType(MediaType.APPLICATION_JSON).content("{}").with(as("LIBRARIAN")));
    }

    @Test
    void memberCanWriteAReviewButNotModerate() throws Exception {
        expectRouted(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON).content("{}").with(as("MEMBER")));
        mockMvc.perform(delete("/api/v1/reviews/1").with(as("MEMBER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void loansAndMembersAreForStaffOnly() throws Exception {
        mockMvc.perform(get("/api/v1/loans").with(as("MEMBER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/members").with(as("MEMBER")))
                .andExpect(status().isForbidden());
        expectRouted(get("/api/v1/loans").with(as("LIBRARIAN")));
    }

    @Test
    void anyAuthenticatedUserCanReadOwnAccount() {
        expectRouted(get("/api/v1/users/me").with(as("MEMBER")));
        expectRouted(get("/api/v1/users/me/loans").with(as("MEMBER")));
    }

    @Test
    void userManagementIsForAdminsOnly() throws Exception {
        mockMvc.perform(get("/api/v1/users").with(as("LIBRARIAN")))
                .andExpect(status().isForbidden());
        expectRouted(get("/api/v1/users").with(as("ADMIN")));
    }

    @Test
    void notificationsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized());
        expectRouted(get("/api/v1/notifications").with(as("MEMBER")));
    }

    @Test
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
