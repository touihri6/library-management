package com.example.review.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewControllerIntegrationTest {

    private static final String NEW_REVIEW = """
            {
              "bookId": 4,
              "reviewerName": "Nour",
              "rating": 5,
              "comment": "Fascinating"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void list_filteredByBookId() throws Exception {
        mockMvc.perform(get("/api/v1/reviews").param("bookId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void average_forBook1() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/books/1/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.average").value(4.7))
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    void average_withoutReviews_returnsZero() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/books/99/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.average").value(0.0))
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void create_withRatingSix_returns400() throws Exception {
        String invalid = NEW_REVIEW.replace("\"rating\": 5", "\"rating\": 6");
        mockMvc.perform(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.rating").exists());
    }

    @Test
    void create_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON).content(NEW_REVIEW))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/reviews/5")).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/reviews/5")).andExpect(status().isNotFound());
    }

    @Test
    void getUnknown_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void list_withUnknownSort_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/reviews").param("sort", "unknownProperty"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid sort property"));
    }

    @Test
    void create_throughGateway_locationUsesForwardedHost() throws Exception {
        mockMvc.perform(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON).content(NEW_REVIEW)
                        .header("X-Forwarded-Host", "localhost:8080")
                        .header("X-Forwarded-Proto", "http"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("http://localhost:8080/api/v1/reviews/")));
    }
}
