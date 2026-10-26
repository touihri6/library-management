package com.example.user.keycloak;

import com.example.user.exception.DuplicateResourceException;
import com.example.user.exception.IdentityProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class KeycloakAdminClient {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAdminClient.class);

    private final RestClient restClient;

    public KeycloakAdminClient(RestClient keycloakAdminRestClient) {
        this.restClient = keycloakAdminRestClient;
    }

    public String createUser(String username, String email, String firstName, String lastName, String password) {
        KeycloakUser user = new KeycloakUser(username, email, firstName, lastName, true, true,
                List.of(new KeycloakCredential("password", password, false)));
        URI location = call(() -> restClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(user)
                .retrieve()
                .toBodilessEntity()
                .getHeaders()
                .getLocation());
        if (location == null) {
            throw new IdentityProviderException("Keycloak did not return the id of user " + username, null);
        }
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public void assignRealmRole(String userId, String role) {
        KeycloakRole realmRole = getRealmRole(role);
        call(() -> restClient.post()
                .uri("/users/{id}/role-mappings/realm", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(realmRole))
                .retrieve()
                .toBodilessEntity());
    }

    public void removeRealmRoles(String userId, Collection<String> roles) {
        List<KeycloakRole> realmRoles = roles.stream().map(this::getRealmRole).toList();
        call(() -> restClient.method(HttpMethod.DELETE)
                .uri("/users/{id}/role-mappings/realm", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(realmRoles)
                .retrieve()
                .toBodilessEntity());
    }

    public void setEnabled(String userId, boolean enabled) {
        call(() -> restClient.put()
                .uri("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("enabled", enabled))
                .retrieve()
                .toBodilessEntity());
    }

    public void deleteUser(String userId) {
        try {
            call(() -> restClient.delete()
                    .uri("/users/{id}", userId)
                    .retrieve()
                    .toBodilessEntity());
        } catch (IdentityProviderException ex) {
            if (ex.getCause() instanceof RestClientResponseException response
                    && response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                log.warn("Keycloak user {} was already deleted", userId);
                return;
            }
            throw ex;
        }
    }

    public void deleteUserQuietly(String userId) {
        try {
            deleteUser(userId);
        } catch (RuntimeException ex) {
            log.error("Could not roll back Keycloak user {}: {}", userId, ex.getMessage());
        }
    }

    private KeycloakRole getRealmRole(String role) {
        return call(() -> restClient.get()
                .uri("/roles/{name}", role)
                .retrieve()
                .body(KeycloakRole.class));
    }

    private <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT)) {
                throw new DuplicateResourceException("A user with the same username or email already exists");
            }
            throw new IdentityProviderException("Keycloak answered " + ex.getStatusCode().value(), ex);
        } catch (RestClientException | OAuth2AuthorizationException ex) {
            throw new IdentityProviderException("Keycloak is unreachable", ex);
        }
    }

    record KeycloakUser(String username, String email, String firstName, String lastName,
                        boolean enabled, boolean emailVerified, List<KeycloakCredential> credentials) {
    }

    record KeycloakCredential(String type, String value, boolean temporary) {
    }

    record KeycloakRole(String id, String name) {
    }
}
