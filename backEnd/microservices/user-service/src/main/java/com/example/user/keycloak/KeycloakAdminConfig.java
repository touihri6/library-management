package com.example.user.keycloak;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class KeycloakAdminConfig {

    @Bean
    public OAuth2AuthorizedClientManager keycloakAdminAuthorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        manager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build());
        return manager;
    }

    @Bean
    public RestClient keycloakAdminRestClient(RestClient.Builder builder,
                                              OAuth2AuthorizedClientManager keycloakAdminAuthorizedClientManager,
                                              KeycloakProperties properties) {
        OAuth2ClientHttpRequestInterceptor interceptor =
                new OAuth2ClientHttpRequestInterceptor(keycloakAdminAuthorizedClientManager);
        interceptor.setClientRegistrationIdResolver(request -> properties.adminClientRegistrationId());
        interceptor.setPrincipalResolver(request ->
                UsernamePasswordAuthenticationToken.unauthenticated(properties.adminClientRegistrationId(), null));
        return builder
                .baseUrl(properties.serverUrl() + "/admin/realms/" + properties.realm())
                .requestInterceptor(interceptor)
                .build();
    }
}
