package com.example.event.service;

import com.example.event.dto.EventRequest;
import com.example.event.dto.EventResponse;
import com.example.event.exception.ResourceNotFoundException;
import com.example.event.mapper.EventMapper;
import com.example.event.mapper.EventMapperImpl;
import com.example.event.model.entity.Event;
import com.example.event.model.enums.EventType;
import com.example.event.repository.EventRepository;
import com.example.event.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    private final EventMapper eventMapper = new EventMapperImpl();

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(eventRepository, eventMapper);
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
