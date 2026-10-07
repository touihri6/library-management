package com.example.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EurekaServerApplicationTest {

    @LocalServerPort
    private int port;

    @Test
    void contextLoads_andDashboardResponds() {
        ResponseEntity<String> response = RestClient.create("http://localhost:" + port)
                .get().uri("/").retrieve().toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
