package com.example.review.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;

@Component
public class CurrentUser {

    private static final Set<String> STAFF_AUTHORITIES = Set.of("ROLE_" + SecurityConfig.LIBRARIAN, "ROLE_" + SecurityConfig.ADMIN);

    public String id() {
        return token().getToken().getSubject();
    }

    public String displayName() {
        Jwt jwt = token().getToken();
        String name = jwt.getClaimAsString("name");
        return StringUtils.hasText(name) ? name : jwt.getClaimAsString("preferred_username");
    }

    public boolean isStaff() {
        return token().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(STAFF_AUTHORITIES::contains);
    }

    private JwtAuthenticationToken token() {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken token) {
            return token;
        }
        throw new AccessDeniedException("An authenticated user is required");
    }
}
