package com.example.user.keycloak;

import com.example.user.exception.DuplicateResourceException;
import com.example.user.exception.IdentityProviderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KeycloakAdminClientTest {

    private static final String BASE = "http://keycloak/admin/realms/library";
    private static final String ROLE_JSON = "{\"id\":\"r-1\",\"name\":\"MEMBER\",\"composite\":false}";

    private MockRestServiceServer server;
    private KeycloakAdminClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new KeycloakAdminClient(builder.build());
    }

    @Test
    void createUser_returnsIdFromLocationHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(BASE + "/users/4f1c-uuid"));
        server.expect(requestTo(BASE + "/users"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.username").value("amine"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.credentials[0].type").value("password"))
                .andExpect(jsonPath("$.credentials[0].temporary").value(false))
                .andRespond(withStatus(HttpStatus.CREATED).headers(headers));

        String id = client.createUser("amine", "amine@mail.com", "Amine", "Ben Ali", "S3cret!pass");

        assertThat(id).isEqualTo("4f1c-uuid");
        server.verify();
    }

    @Test
    void createUser_conflict_throwsDuplicate() {
        server.expect(requestTo(BASE + "/users"))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        assertThatThrownBy(() -> client.createUser("amine", "amine@mail.com", "Amine", "Ben Ali", "S3cret!pass"))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createUser_serverError_throwsIdentityProviderException() {
        server.expect(requestTo(BASE + "/users"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> client.createUser("amine", "amine@mail.com", "Amine", "Ben Ali", "S3cret!pass"))
                .isInstanceOf(IdentityProviderException.class)
                .hasMessage("Keycloak answered 403");
    }

    @Test
    void assignRealmRole_readsRoleThenMapsIt() {
        server.expect(requestTo(BASE + "/roles/MEMBER"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(ROLE_JSON, MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/users/u-1/role-mappings/realm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("[{\"id\":\"r-1\",\"name\":\"MEMBER\"}]"))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        client.assignRealmRole("u-1", "MEMBER");

        server.verify();
    }

    @Test
    void removeRealmRoles_sendsDeleteWithRoles() {
        server.expect(requestTo(BASE + "/roles/MEMBER"))
                .andRespond(withSuccess(ROLE_JSON, MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/users/u-1/role-mappings/realm"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(content().json("[{\"id\":\"r-1\",\"name\":\"MEMBER\"}]"))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        client.removeRealmRoles("u-1", List.of("MEMBER"));

        server.verify();
    }

    @Test
    void setEnabled_putsEnabledFlag() {
        server.expect(requestTo(BASE + "/users/u-1"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().json("{\"enabled\":false}", true))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        client.setEnabled("u-1", false);

        server.verify();
    }

    @Test
    void deleteUser_alreadyDeleted_isIgnored() {
        server.expect(requestTo(BASE + "/users/u-1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatCode(() -> client.deleteUser("u-1")).doesNotThrowAnyException();
    }

    @Test
    void deleteUserQuietly_swallowsErrors() {
        server.expect(requestTo(BASE + "/users/u-1"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatCode(() -> client.deleteUserQuietly("u-1")).doesNotThrowAnyException();
    }
}
