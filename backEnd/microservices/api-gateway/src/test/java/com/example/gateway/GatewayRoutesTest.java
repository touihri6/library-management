package com.example.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.boot.tomcat.TomcatWebServer;
import org.springframework.cloud.gateway.server.mvc.config.GatewayMvcProperties;
import org.springframework.cloud.gateway.server.mvc.config.RouteProperties;
import org.springframework.cloud.gateway.server.mvc.filter.XForwardedRequestHeadersFilter;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutesTest {

    @Autowired
    private GatewayMvcProperties gatewayProperties;

    @Autowired
    private WebServerApplicationContext context;

    @Test
    void allRoutesAreDefined() {
        Map<String, String> routes = gatewayProperties.getRoutes()
                .stream()
                .collect(Collectors.toMap(RouteProperties::getId, route -> route.getUri().toString()));

        assertThat(routes).containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
                Map.entry("book-service", "lb://BOOK-SERVICE"),
                Map.entry("book-service-docs", "lb://BOOK-SERVICE"),
                Map.entry("member-service", "lb://MEMBER-SERVICE"),
                Map.entry("member-service-docs", "lb://MEMBER-SERVICE"),
                Map.entry("loan-service", "lb://LOAN-SERVICE"),
                Map.entry("loan-service-docs", "lb://LOAN-SERVICE"),
                Map.entry("review-service", "lb://REVIEW-SERVICE"),
                Map.entry("review-service-docs", "lb://REVIEW-SERVICE"),
                Map.entry("event-service", "lb://EVENT-SERVICE"),
                Map.entry("event-service-docs", "lb://EVENT-SERVICE"),
                Map.entry("author-service", "lb://AUTHOR-SERVICE"),
                Map.entry("author-service-docs", "lb://AUTHOR-SERVICE"),
                Map.entry("notification-service", "lb://NOTIFICATION-SERVICE"),
                Map.entry("notification-service-docs", "lb://NOTIFICATION-SERVICE"),
                Map.entry("user-service", "lb://USER-SERVICE"),
                Map.entry("user-service-docs", "lb://USER-SERVICE")));
    }

    @Test
    void runsOnTomcat() {
        assertThat(context.getWebServer()).isInstanceOf(TomcatWebServer.class);
    }

    @Test
    void forwardsXForwardedHeadersToServices() {
        assertThat(context.getBeansOfType(XForwardedRequestHeadersFilter.class)).isNotEmpty();
    }
}
