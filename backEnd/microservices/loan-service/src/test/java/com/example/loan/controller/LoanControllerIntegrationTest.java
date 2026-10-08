package com.example.loan.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoanControllerIntegrationTest {

    private static final String NEW_LOAN = """
            {
              "bookId": 6,
              "memberId": 4,
              "loanDate": "2026-10-01",
              "dueDate": "2026-10-15"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void list_filteredByStatus() throws Exception {
        mockMvc.perform(get("/api/v1/loans").param("status", "ONGOING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void list_filteredByMemberId() throws Exception {
        mockMvc.perform(get("/api/v1/loans").param("memberId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void create_returns201AndOngoing() throws Exception {
        mockMvc.perform(post("/api/v1/loans").contentType(MediaType.APPLICATION_JSON).content(NEW_LOAN))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("ONGOING"));
    }

    @Test
    void create_ignoresClientStatus() throws Exception {
        String withStatus = NEW_LOAN.replace("\"dueDate\": \"2026-10-15\"",
                "\"dueDate\": \"2026-10-15\", \"status\": \"RETURNED\", \"returnDate\": \"2026-10-02\"");
        mockMvc.perform(post("/api/v1/loans").contentType(MediaType.APPLICATION_JSON).content(withStatus))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ONGOING"))
                .andExpect(jsonPath("$.returnDate").doesNotExist());
    }

    @Test
    void create_withDueDateBeforeLoanDate_returns400() throws Exception {
        String invalid = NEW_LOAN.replace("2026-10-15", "2026-09-01");
        mockMvc.perform(post("/api/v1/loans").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid dates"));
    }

    @Test
    void returnLoan_returns200AndReturned() throws Exception {
        mockMvc.perform(patch("/api/v1/loans/1/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.returnDate").value(LocalDate.now().toString()));
    }

    @Test
    void returnLoan_twice_returns409() throws Exception {
        mockMvc.perform(patch("/api/v1/loans/4/return"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Invalid operation"));
    }

    @Test
    void getUnknown_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/loans/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void list_withUnknownSort_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/loans").param("sort", "unknownProperty"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid sort property"));
    }

    @Test
    void create_throughGateway_locationUsesForwardedHost() throws Exception {
        mockMvc.perform(post("/api/v1/loans").contentType(MediaType.APPLICATION_JSON).content(NEW_LOAN)
                        .header("X-Forwarded-Host", "localhost:8080")
                        .header("X-Forwarded-Proto", "http"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("http://localhost:8080/api/v1/loans/")));
    }
}
