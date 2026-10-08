package com.example.event.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static String event(LocalDateTime date, int capacity) {
        return """
                {
                  "title": "Atelier écriture",
                  "description": "Initiation à l'écriture de nouvelles",
                  "type": "WORKSHOP",
                  "eventDate": "%s",
                  "location": "Salle B",
                  "capacity": %d
                }
                """.formatted(date.truncatedTo(ChronoUnit.SECONDS), capacity);
    }

    @Test
    void list_filteredByType() throws Exception {
        mockMvc.perform(get("/api/v1/events").param("type", "READING_CLUB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void create_withPastDate_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/events").contentType(MediaType.APPLICATION_JSON)
                        .content(event(LocalDateTime.now().minusDays(1), 10)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.eventDate").exists());
    }

    @Test
    void create_withZeroCapacity_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/events").contentType(MediaType.APPLICATION_JSON)
                        .content(event(LocalDateTime.now().plusDays(30), 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.capacity").exists());
    }

    @Test
    void create_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/events").contentType(MediaType.APPLICATION_JSON)
                        .content(event(LocalDateTime.now().plusDays(30), 15)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.type").value("WORKSHOP"));
    }

    @Test
    void update_returns200() throws Exception {
        mockMvc.perform(put("/api/v1/events/1").contentType(MediaType.APPLICATION_JSON)
                        .content(event(LocalDateTime.now().plusDays(60), 40)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacity").value(40));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/events/5")).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/events/5")).andExpect(status().isNotFound());
    }

    @Test
    void getUnknown_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/events/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void list_withUnknownSort_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/events").param("sort", "unknownProperty"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid sort property"));
    }

    @Test
    void create_throughGateway_locationUsesForwardedHost() throws Exception {
        mockMvc.perform(post("/api/v1/events").contentType(MediaType.APPLICATION_JSON).content(event(LocalDateTime.now().plusDays(30), 15))
                        .header("X-Forwarded-Host", "localhost:8080")
                        .header("X-Forwarded-Proto", "http"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("http://localhost:8080/api/v1/events/")));
    }
}
