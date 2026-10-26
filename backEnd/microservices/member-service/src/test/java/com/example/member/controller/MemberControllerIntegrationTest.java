package com.example.member.controller;

import com.example.member.client.LoanClient;
import com.example.member.dto.LoanSummary;
import com.example.member.dto.PageResponse;
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

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
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
class MemberControllerIntegrationTest {

    private static final String NEW_MEMBER = """
            {
              "firstName": "Nour",
              "lastName": "Jaziri",
              "email": "nour@mail.com",
              "phone": "55123456",
              "membershipType": "PREMIUM",
              "joinDate": "2026-01-15"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanClient loanClient;

    @BeforeEach
    void setUp() {
        when(loanClient.findByMember(anyLong(), anyInt())).thenReturn(new PageResponse<>(List.of(), 0, 100, 0, 0, true));
    }

    private static PageResponse<LoanSummary> ongoingLoan() {
        return new PageResponse<>(List.of(new LoanSummary(10L, 1L, LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 15), null, "ONGOING")), 0, 100, 1, 1, true);
    }

    @Test
    void list_filteredByEmail_ignoresCase() throws Exception {
        mockMvc.perform(get("/api/v1/members").param("email", "AMINE@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].lastName").value("Ben Ali"));
    }

    @Test
    void loans_returnsMemberWithLoansFromLoanService() throws Exception {
        when(loanClient.findByMember(1L, 100)).thenReturn(ongoingLoan());

        mockMvc.perform(get("/api/v1/members/1/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.member.email").value("amine@mail.com"))
                .andExpect(jsonPath("$.loans[0].status").value("ONGOING"));
    }

    @Test
    void delete_memberWithOngoingLoan_returns409() throws Exception {
        when(loanClient.findByMember(1L, 100)).thenReturn(ongoingLoan());

        mockMvc.perform(delete("/api/v1/members/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Member 1 still has 1 ongoing loan(s)"));
    }

    @Test
    void list_filteredByMembershipType() throws Exception {
        mockMvc.perform(get("/api/v1/members").param("membershipType", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void list_filteredByLastName() throws Exception {
        mockMvc.perform(get("/api/v1/members").param("lastName", "trab"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("sara@mail.com"));
    }

    @Test
    void create_returns201WithLocation() throws Exception {
        mockMvc.perform(post("/api/v1/members").contentType(MediaType.APPLICATION_JSON).content(NEW_MEMBER))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void create_withDuplicateEmail_returns409() throws Exception {
        String duplicate = NEW_MEMBER.replace("nour@mail.com", "amine@mail.com");
        mockMvc.perform(post("/api/v1/members").contentType(MediaType.APPLICATION_JSON).content(duplicate))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate resource"));
    }

    @Test
    void create_withInvalidEmail_returns400() throws Exception {
        String invalid = NEW_MEMBER.replace("nour@mail.com", "not-an-email");
        mockMvc.perform(post("/api/v1/members").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void update_keepingOwnEmail_returns200() throws Exception {
        String update = NEW_MEMBER.replace("nour@mail.com", "sara@mail.com").replace("Jaziri", "Trabelsi");
        mockMvc.perform(put("/api/v1/members/2").contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("sara@mail.com"));
    }

    @Test
    void getUnknown_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/members/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/members/5")).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/members/5")).andExpect(status().isNotFound());
    }

    @Test
    void list_withUnknownSort_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/members").param("sort", "unknownProperty"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid sort property"));
    }

    @Test
    void create_throughGateway_locationUsesForwardedHost() throws Exception {
        mockMvc.perform(post("/api/v1/members").contentType(MediaType.APPLICATION_JSON).content(NEW_MEMBER)
                        .header("X-Forwarded-Host", "localhost:8080")
                        .header("X-Forwarded-Proto", "http"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("http://localhost:8080/api/v1/members/")));
    }
}
