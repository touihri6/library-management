package com.example.event.controller;

import com.example.event.client.MemberClient;
import com.example.event.client.NotificationClient;
import com.example.event.dto.MemberContact;
import com.example.event.dto.PageResponse;
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberClient memberClient;

    @MockitoBean
    private NotificationClient notificationClient;

    @Test
    void announce_notifiesEveryMember() throws Exception {
        when(memberClient.findAll(0, 100)).thenReturn(new PageResponse<>(List.of(
                new MemberContact(1L, "Amine", "Ben Ali", "amine@mail.com"),
                new MemberContact(2L, "Nour", "Haddad", "nour@mail.com")), 0, 100, 2, 1, true));

        mockMvc.perform(post("/api/v1/events/1/announce"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.recipients").value(2))
                .andExpect(jsonPath("$.sent").value(2))
                .andExpect(jsonPath("$.failed").value(0));
        verify(notificationClient, times(2)).create(any());
    }

    @Test
    void announce_whenMemberServiceDown_returns503() throws Exception {
        Request request = Request.create(Request.HttpMethod.GET, "/api/v1/members", Map.of(), null, StandardCharsets.UTF_8, null);
        when(memberClient.findAll(anyInt(), anyInt()))
                .thenThrow(new FeignException.ServiceUnavailable("503", request, null, Map.of()));

        mockMvc.perform(post("/api/v1/events/1/announce"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("member-service is unavailable"));
    }

    @Test
    void announce_unknownEvent_returns404() throws Exception {
        mockMvc.perform(post("/api/v1/events/999/announce"))
                .andExpect(status().isNotFound());
    }

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
