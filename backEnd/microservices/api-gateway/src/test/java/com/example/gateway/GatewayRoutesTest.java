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

        assertThat(routes).containsExactlyInAnyOrderEntriesOf(Map.of(
                "book-service", "lb://BOOK-SERVICE",
                "notification-service", "lb://NOTIFICATION-SERVICE"));
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
