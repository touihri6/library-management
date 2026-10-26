package com.example.user.client;

import com.example.user.exception.IdentityProviderException;
import com.example.user.keycloak.KeycloakProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;

@Component
public class ServiceTokenInterceptor implements RequestInterceptor {

    private final OAuth2AuthorizedClientManager clientManager;
    private final KeycloakProperties properties;

    public ServiceTokenInterceptor(OAuth2AuthorizedClientManager keycloakAdminAuthorizedClientManager,
                                   KeycloakProperties properties) {
        this.clientManager = keycloakAdminAuthorizedClientManager;
        this.properties = properties;
    }

    @Override
    public void apply(RequestTemplate template) {
        OAuth2AuthorizedClient client = clientManager.authorize(OAuth2AuthorizeRequest
                .withClientRegistrationId(properties.adminClientRegistrationId())
                .principal(properties.adminClientRegistrationId())
                .build());
        if (client == null) {
            throw new IdentityProviderException("Keycloak did not issue a service token", null);
        }
        template.header(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken().getTokenValue());
    }
}
