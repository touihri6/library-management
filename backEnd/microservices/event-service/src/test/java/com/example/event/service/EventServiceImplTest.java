package com.example.event.service;

import com.example.event.client.MemberClient;
import com.example.event.client.NotificationClient;
import com.example.event.dto.AnnouncementResponse;
import com.example.event.dto.EventRequest;
import com.example.event.dto.EventResponse;
import com.example.event.dto.MemberContact;
import com.example.event.dto.NotificationRequest;
import com.example.event.dto.PageResponse;
import com.example.event.exception.InvalidOperationException;
import com.example.event.exception.ResourceNotFoundException;
import com.example.event.mapper.EventMapper;
import com.example.event.mapper.EventMapperImpl;
import com.example.event.model.entity.Event;
import com.example.event.model.enums.EventType;
import com.example.event.repository.EventRepository;
import com.example.event.service.impl.EventServiceImpl;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private MemberClient memberClient;

    @Mock
    private NotificationClient notificationClient;

    private final EventMapper eventMapper = new EventMapperImpl();

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(eventRepository, eventMapper, memberClient, notificationClient);
    }

    @Test
    void announce_sendsOneEventAnnouncementPerMemberAcrossPages() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event(LocalDateTime.now().plusDays(7))));
        when(memberClient.findAll(0, 100)).thenReturn(members(false, contact(1L, "amine@mail.com")));
        when(memberClient.findAll(1, 100)).thenReturn(members(true, contact(2L, "nour@mail.com")));

        AnnouncementResponse result = eventService.announce(1L);

        assertThat(result.recipients()).isEqualTo(2);
        assertThat(result.sent()).isEqualTo(2);
        assertThat(result.failed()).isZero();
        ArgumentCaptor<NotificationRequest> sent = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationClient, times(2)).create(sent.capture());
        assertThat(sent.getAllValues()).extracting(NotificationRequest::recipient)
                .containsExactly("amine@mail.com", "nour@mail.com");
        assertThat(sent.getValue().type()).isEqualTo("EVENT_ANNOUNCEMENT");
        assertThat(sent.getValue().message()).startsWith("New reading club at the library: \"Dune club\" on ");
    }

    @Test
    void announce_countsFailedNotificationsWithoutStopping() {
        Request request = Request.create(Request.HttpMethod.POST, "/api/v1/notifications", Map.of(), null, StandardCharsets.UTF_8, null);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event(LocalDateTime.now().plusDays(7))));
        when(memberClient.findAll(0, 100)).thenReturn(members(true, contact(1L, "amine@mail.com"), contact(2L, "nour@mail.com")));
        when(notificationClient.create(argThat(n -> n != null && n.recipient().equals("amine@mail.com"))))
                .thenThrow(new FeignException.ServiceUnavailable("503", request, null, Map.of()));

        AnnouncementResponse result = eventService.announce(1L);

        assertThat(result.sent()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(1);
    }

    @Test
    void announce_pastEvent_throwsInvalidOperation() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event(LocalDateTime.now().minusDays(1))));

        assertThatThrownBy(() -> eventService.announce(1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessage("Event 1 is already over");
        verifyNoInteractions(memberClient, notificationClient);
    }

    private static Event event(LocalDateTime date) {
        Event event = new Event();
        event.setId(1L);
        event.setTitle("Dune club");
        event.setType(EventType.READING_CLUB);
        event.setEventDate(date);
        event.setLocation("Room A");
        event.setCapacity(20);
        return event;
    }

    private static MemberContact contact(Long id, String email) {
        return new MemberContact(id, "First", "Last", email);
    }

    private static PageResponse<MemberContact> members(boolean last, MemberContact... members) {
        return new PageResponse<>(List.of(members), 0, 100, members.length, 2, last);
    }

    @Test
    void create_savesAndReturnsResponse() {
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setId(1L);
            return event;
        });

        EventResponse response = eventService.create(new EventRequest("Club de lecture", "Dune", EventType.READING_CLUB,
                LocalDateTime.now().plusDays(30), "Salle A", 20));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.type()).isEqualTo(EventType.READING_CLUB);
        assertThat(response.capacity()).isEqualTo(20);
    }

    @Test
    void findById_unknown_throwsNotFound() {
        when(eventRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.findById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }
}
