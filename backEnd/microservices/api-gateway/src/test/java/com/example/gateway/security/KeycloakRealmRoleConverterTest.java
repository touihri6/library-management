package com.example.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    private static Jwt jwt(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("4f1c")
                .issuedAt(Instant.parse("2026-10-26T10:00:00Z"))
                .expiresAt(Instant.parse("2026-10-26T10:05:00Z"));
        claims.forEach(builder::claim);
        return builder.build();
    }

    @Test
    void realmRolesBecomeSpringRoles() {
        Jwt token = jwt(Map.of("realm_access", Map.of("roles", List.of("LIBRARIAN", "offline_access"))));

        List<String> authorities = converter.convert(token).stream().map(GrantedAuthority::getAuthority).toList();

        assertThat(authorities).containsExactly("ROLE_LIBRARIAN", "ROLE_offline_access");
    }

    @Test
    void tokenWithoutRealmAccessHasNoRole() {
        assertThat(converter.convert(jwt(Map.of("preferred_username", "member")))).isEmpty();
    }

    @Test
    void realmAccessWithoutRolesHasNoRole() {
        assertThat(converter.convert(jwt(Map.of("realm_access", Map.of())))).isEmpty();
    }
}
