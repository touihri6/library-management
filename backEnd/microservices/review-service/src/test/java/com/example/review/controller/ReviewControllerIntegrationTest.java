package com.example.review.controller;

import com.example.review.client.BookClient;
import com.jayway.jsonpath.JsonPath;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "LIBRARIAN")
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

    @MockitoBean
    private BookClient bookClient;

    private static RequestPostProcessor as(String subject, String name, String role) {
        return jwt().jwt(token -> token.subject(subject).claim("name", name))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private static RequestPostProcessor librarian() {
        return as("sara-id", "Sara Trabelsi", "LIBRARIAN");
    }

    private static RequestPostProcessor member(String subject, String name) {
        return as(subject, name, "MEMBER");
    }

    private static Request request(String url) {
        return Request.create(Request.HttpMethod.GET, url, Map.of(), null, StandardCharsets.UTF_8, null);
    }

    @Test
    void create_forUnknownBook_returns404() throws Exception {
        when(bookClient.findById(4L)).thenThrow(new FeignException.NotFound("404", request("/api/v1/books/4"), null, Map.of()));

        mockMvc.perform(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON).content(NEW_REVIEW))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Book 4 not found"));
    }

    @Test
    void create_whenBookServiceDown_returns503() throws Exception {
        when(bookClient.findById(4L))
                .thenThrow(new FeignException.ServiceUnavailable("503", request("/api/v1/books/4"), null, Map.of()));

        mockMvc.perform(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON).content(NEW_REVIEW))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("book-service is unavailable"));
    }

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
        mockMvc.perform(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON).content(NEW_REVIEW).with(librarian()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/reviews/5").with(librarian())).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/reviews/5")).andExpect(status().isNotFound());
    }

    @Test
    void create_byMember_takesTheNameAndIdFromTheToken() throws Exception {
        mockMvc.perform(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON).content(NEW_REVIEW)
                        .with(member("nour-id", "Nour Haddad")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("nour-id"))
                .andExpect(jsonPath("$.reviewerName").value("Nour Haddad"))
                .andExpect(jsonPath("$.createdBy").value("nour-id"));
    }

    @Test
    void delete_reviewOfSomeoneElse_byMember_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/reviews/5").with(member("nour-id", "Nour Haddad")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/reviews/5")).andExpect(status().isOk());
    }

    @Test
    void delete_ownReview_byMember_returns204() throws Exception {
        String body = mockMvc.perform(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON).content(NEW_REVIEW)
                        .with(member("nour-id", "Nour Haddad")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(body, "$.id");

        mockMvc.perform(delete("/api/v1/reviews/" + id).with(member("nour-id", "Nour Haddad")))
                .andExpect(status().isNoContent());
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
                        .with(librarian())
                        .header("X-Forwarded-Host", "localhost:8080")
                        .header("X-Forwarded-Proto", "http"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("http://localhost:8080/api/v1/reviews/")));
    }
}
