package com.example.author.controller;

import com.example.author.client.BookClient;
import com.example.author.dto.BookSummary;
import com.example.author.dto.PageResponse;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
class AuthorControllerIntegrationTest {

    private static final String NEW_AUTHOR = """
            {
              "firstName": "Isaac",
              "lastName": "Asimov",
              "nationality": "American",
              "birthYear": 1920,
              "biography": "Science fiction writer"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookClient bookClient;

    @BeforeEach
    void setUp() {
        when(bookClient.findByAuthor(anyString(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(), 0, 100, 0, 0, true));
    }

    @Test
    void books_returnsAuthorWithTheirBooksFromBookService() throws Exception {
        when(bookClient.findByAuthor("Frank Herbert", 100)).thenReturn(new PageResponse<>(List.of(
                new BookSummary(1L, "Dune", "Frank Herbert", "978-0441172719", "SCIENCE_FICTION", 1965, 4, true)),
                0, 100, 1, 1, true));

        mockMvc.perform(get("/api/v1/authors/1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author.lastName").value("Herbert"))
                .andExpect(jsonPath("$.books[0].title").value("Dune"));
    }

    @Test
    void books_whenBookServiceDown_returns503() throws Exception {
        Request request = Request.create(Request.HttpMethod.GET, "/api/v1/books", Map.of(), null, StandardCharsets.UTF_8, null);
        when(bookClient.findByAuthor(anyString(), anyInt()))
                .thenThrow(new FeignException.ServiceUnavailable("503", request, null, Map.of()));

        mockMvc.perform(get("/api/v1/authors/1/books"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("book-service is unavailable"));
    }

    @Test
    void delete_authorWithBooks_returns409() throws Exception {
        when(bookClient.findByAuthor("Frank Herbert", 100)).thenReturn(new PageResponse<>(List.of(
                new BookSummary(1L, "Dune", "Frank Herbert", "978-0441172719", "SCIENCE_FICTION", 1965, 4, true)),
                0, 100, 1, 1, true));

        mockMvc.perform(delete("/api/v1/authors/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Author 1 still has 1 book(s) in the catalog"));
    }

    @Test
    void list_filteredByNationality() throws Exception {
        mockMvc.perform(get("/api/v1/authors").param("nationality", "british"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void list_filteredByLastName() throws Exception {
        mockMvc.perform(get("/api/v1/authors").param("lastName", "tolk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].lastName").value("Tolkien"));
    }

    @Test
    void create_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/authors").contentType(MediaType.APPLICATION_JSON).content(NEW_AUTHOR))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void create_withBlankLastName_returns400() throws Exception {
        String invalid = NEW_AUTHOR.replace("\"Asimov\"", "\"\"");
        mockMvc.perform(post("/api/v1/authors").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.lastName").exists());
    }

    @Test
    void update_returns200() throws Exception {
        mockMvc.perform(put("/api/v1/authors/1").contentType(MediaType.APPLICATION_JSON).content(NEW_AUTHOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Asimov"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/authors/5")).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/authors/5")).andExpect(status().isNotFound());
    }

    @Test
    void getUnknown_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/authors/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void list_withUnknownSort_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/authors").param("sort", "unknownProperty"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid sort property"));
    }

    @Test
    void create_throughGateway_locationUsesForwardedHost() throws Exception {
        mockMvc.perform(post("/api/v1/authors").contentType(MediaType.APPLICATION_JSON).content(NEW_AUTHOR)
                        .header("X-Forwarded-Host", "localhost:8080")
                        .header("X-Forwarded-Proto", "http"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("http://localhost:8080/api/v1/authors/")));
    }
}
