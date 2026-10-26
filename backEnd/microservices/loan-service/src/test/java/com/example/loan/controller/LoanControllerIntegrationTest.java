package com.example.loan.controller;

import com.example.loan.client.BookClient;
import com.example.loan.client.MemberClient;
import com.example.loan.dto.BookResponse;
import com.example.loan.dto.MemberResponse;
import com.example.loan.messaging.LoanEventPublisher;
import com.example.loan.model.entity.Loan;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    private static final BookResponse AVAILABLE_BOOK =
            new BookResponse(6L, "Effective Java", "Joshua Bloch", "978-0134685991", 1, true);
    private static final MemberResponse MEMBER =
            new MemberResponse(4L, "Amine", "Ben Ali", "amine@mail.com", "STUDENT");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookClient bookClient;

    @MockitoBean
    private MemberClient memberClient;

    @MockitoBean
    private LoanEventPublisher loanEventPublisher;

    @BeforeEach
    void setUp() {
        when(bookClient.findById(anyLong())).thenReturn(AVAILABLE_BOOK);
        when(memberClient.findById(anyLong())).thenReturn(MEMBER);
    }

    private static FeignException.NotFound notFound(String url) {
        Request request = Request.create(Request.HttpMethod.GET, url, Map.of(), null, StandardCharsets.UTF_8, null);
        return new FeignException.NotFound("404", request, null, Map.of());
    }

    private static FeignException.ServiceUnavailable unavailable(String url) {
        Request request = Request.create(Request.HttpMethod.GET, url, Map.of(), null, StandardCharsets.UTF_8, null);
        return new FeignException.ServiceUnavailable("503", request, null, Map.of());
    }

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
        verify(loanEventPublisher).publishLoanCreated(any(Loan.class));
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
        verify(loanEventPublisher).publishLoanReturned(any(Loan.class));
    }

    @Test
    void returnLoan_twice_returns409() throws Exception {
        mockMvc.perform(patch("/api/v1/loans/4/return"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Invalid operation"));
        verifyNoInteractions(loanEventPublisher);
    }

    @Test
    void update_withDifferentBook_returns409() throws Exception {
        mockMvc.perform(put("/api/v1/loans/1").contentType(MediaType.APPLICATION_JSON).content(NEW_LOAN))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("The book of loan 1 cannot be changed"));
    }

    @Test
    void delete_ongoingLoan_returns204AndPublishesCancelled() throws Exception {
        mockMvc.perform(delete("/api/v1/loans/1"))
                .andExpect(status().isNoContent());
        verify(loanEventPublisher).publishLoanCancelled(any(Loan.class));
    }

    @Test
    void delete_returnedLoan_returns204WithoutEvent() throws Exception {
        mockMvc.perform(delete("/api/v1/loans/4"))
                .andExpect(status().isNoContent());
        verifyNoInteractions(loanEventPublisher);
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

    @Test
    void create_withUnknownBook_returns404() throws Exception {
        when(bookClient.findById(6L)).thenThrow(notFound("/api/v1/books/6"));

        mockMvc.perform(post("/api/v1/loans").contentType(MediaType.APPLICATION_JSON).content(NEW_LOAN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Book 6 not found"));
    }

    @Test
    void create_withUnknownMember_returns404() throws Exception {
        when(memberClient.findById(4L)).thenThrow(notFound("/api/v1/members/4"));

        mockMvc.perform(post("/api/v1/loans").contentType(MediaType.APPLICATION_JSON).content(NEW_LOAN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Member 4 not found"));
    }

    @Test
    void create_withUnavailableBook_returns409() throws Exception {
        when(bookClient.findById(6L))
                .thenReturn(new BookResponse(6L, "Effective Java", "Joshua Bloch", "978-0134685991", 0, false));

        mockMvc.perform(post("/api/v1/loans").contentType(MediaType.APPLICATION_JSON).content(NEW_LOAN))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Book 6 has no available copy"));
    }

    @Test
    void create_whenBookServiceDown_returns503() throws Exception {
        when(bookClient.findById(6L)).thenThrow(unavailable("/api/v1/books/6"));

        mockMvc.perform(post("/api/v1/loans").contentType(MediaType.APPLICATION_JSON).content(NEW_LOAN))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Service unavailable"));
    }

    @Test
    void details_returnsLoanWithBookAndMember() throws Exception {
        mockMvc.perform(get("/api/v1/loans/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.book.title").value("Effective Java"))
                .andExpect(jsonPath("$.member.email").value("amine@mail.com"));
    }

    @Test
    void details_ofUnknownLoan_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/loans/999/details"))
                .andExpect(status().isNotFound());
    }
}
