package com.example.review.security;

import com.example.review.client.BookClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookClient bookClient;

    private static RequestPostProcessor as(String... roles) {
        return jwt().jwt(token -> token.claim("preferred_username", "user")
                        .claim("realm_access", Map.of("roles", List.of(roles))))
                .authorities(new KeycloakRealmRoleConverter());
    }

    @Test
    void swaggerIsPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    private static final String NEW_REVIEW = """
            {"bookId": 4, "reviewerName": "Nour", "rating": 5, "comment": "Fascinating"}
            """;

    @Test
    void anonymousCanReadReviews() throws Exception {
        mockMvc.perform(get("/api/v1/reviews"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/reviews/books/1/average"))
                .andExpect(status().isOk());
    }

    @Test
    void writeWithoutTokenIsRejectedWith401() throws Exception {
        mockMvc.perform(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON).content(NEW_REVIEW))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", startsWith("Bearer")));
    }

    @Test
    void memberCanWriteAReview() throws Exception {
        mockMvc.perform(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON).content(NEW_REVIEW).with(as("MEMBER")))
                .andExpect(status().isCreated());
    }

    @Test
    void memberCannotModerateReviews() throws Exception {
        mockMvc.perform(put("/api/v1/reviews/1").contentType(MediaType.APPLICATION_JSON).content(NEW_REVIEW).with(as("MEMBER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/reviews/1").with(as("MEMBER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void librarianCanModerateReviews() throws Exception {
        mockMvc.perform(delete("/api/v1/reviews/1").with(as("LIBRARIAN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void userWithoutRoleCannotWriteAReview() throws Exception {
        mockMvc.perform(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON).content(NEW_REVIEW).with(as()))
                .andExpect(status().isForbidden());
    }
}
