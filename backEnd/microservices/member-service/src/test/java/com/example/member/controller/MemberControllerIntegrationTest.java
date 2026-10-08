package com.example.member.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
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
