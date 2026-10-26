package com.example.book.security;

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BearerTokenRelayInterceptorTest {

    private final BearerTokenRelayInterceptor interceptor = new BearerTokenRelayInterceptor();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void relaysTheCallerJwt() {
        Jwt jwt = Jwt.withTokenValue("caller-token")
                .header("alg", "RS256")
                .subject("kc-1")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertThat(template.headers().get(HttpHeaders.AUTHORIZATION)).containsExactly("Bearer caller-token");
    }

    @Test
    void addsNothingWithoutJwt() {
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertThat(template.headers()).doesNotContainKey(HttpHeaders.AUTHORIZATION);
    }

    @Test
    void addsNothingForNonJwtAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("user", null, List.of()));
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertThat(template.headers()).doesNotContainKey(HttpHeaders.AUTHORIZATION);
    }
}
