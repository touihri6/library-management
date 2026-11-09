package com.example.member.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public String id() {
        return jwt().getSubject();
    }

    public String email() {
        return jwt().getClaimAsString("email");
    }

    private Jwt jwt() {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken token) {
            return token.getToken();
        }
        throw new AccessDeniedException("An authenticated user is required");
    }
}
