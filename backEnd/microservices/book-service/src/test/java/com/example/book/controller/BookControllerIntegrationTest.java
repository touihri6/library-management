package com.example.book.controller;

import com.example.book.client.ReviewClient;
import com.example.book.dto.RatingSummary;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "LIBRARIAN")
class BookControllerIntegrationTest {

    private static final String NEW_BOOK = """
            {
              "title": "Neuromancer",
              "author": "William Gibson",
              "isbn": "978-0441569595",
              "genre": "SCIENCE_FICTION",
              "publicationYear": 1984,
              "price": 10.50,
              "availableCopies": 2
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewClient reviewClient;

    @Test
    void listBooks_filteredByGenre() throws Exception {
        mockMvc.perform(get("/api/v1/books").param("genre", "TECHNOLOGY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void listBooks_filteredByAuthor() throws Exception {
        mockMvc.perform(get("/api/v1/books").param("author", "tolk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("The Hobbit"));
    }

    @Test
    void createThenGetBook() throws Exception {
        mockMvc.perform(post("/api/v1/books").contentType(MediaType.APPLICATION_JSON).content(NEW_BOOK))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void createBook_withInvalidPayload_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/books").contentType(MediaType.APPLICATION_JSON).content("{\"title\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.isbn").exists());
    }

    @Test
    void createBook_withExistingIsbn_returns409() throws Exception {
        String duplicate = NEW_BOOK.replace("978-0441569595", "978-0441172719");
        mockMvc.perform(post("/api/v1/books").contentType(MediaType.APPLICATION_JSON).content(duplicate))
                .andExpect(status().isConflict());
    }

    @Test
    void updateBook() throws Exception {
        String update = NEW_BOOK.replace("978-0441569595", "978-0441172719").replace("Neuromancer", "Dune (new edition)");
        mockMvc.perform(put("/api/v1/books/1").contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dune (new edition)"));
    }

    @Test
    void deleteBook_thenGetReturns404() throws Exception {
        mockMvc.perform(delete("/api/v1/books/2")).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/books/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void openApiDocIsExposed() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Book Service API"));
    }

    @Test
    void list_withUnknownSort_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/books").param("sort", "unknownProperty"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid sort property"));
    }

    @Test
    void create_throughGateway_locationUsesForwardedHost() throws Exception {
        mockMvc.perform(post("/api/v1/books").contentType(MediaType.APPLICATION_JSON).content(NEW_BOOK)
                        .header("X-Forwarded-Host", "localhost:8080")
                        .header("X-Forwarded-Proto", "http"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("http://localhost:8080/api/v1/books/")));
    }

    @Test
    void details_returnsBookWithRating() throws Exception {
        when(reviewClient.findAverage(1L)).thenReturn(new RatingSummary(1L, 4.5, 2));

        mockMvc.perform(get("/api/v1/books/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.book.title").value("Dune"))
                .andExpect(jsonPath("$.rating.average").value(4.5))
                .andExpect(jsonPath("$.rating.count").value(2));
    }

    @Test
    void details_whenReviewServiceDown_returnsBookWithoutRating() throws Exception {
        Request request = Request.create(Request.HttpMethod.GET, "/api/v1/reviews/books/1/average",
                Map.of(), null, StandardCharsets.UTF_8, null);
        when(reviewClient.findAverage(1L))
                .thenThrow(new FeignException.ServiceUnavailable("503", request, null, Map.of()));

        mockMvc.perform(get("/api/v1/books/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.book.title").value("Dune"))
                .andExpect(jsonPath("$.rating").doesNotExist());
    }

    @Test
    void details_ofUnknownBook_returns404WithoutCallingReviewService() throws Exception {
        mockMvc.perform(get("/api/v1/books/999/details"))
                .andExpect(status().isNotFound());
        verifyNoInteractions(reviewClient);
    }
}
