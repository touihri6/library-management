package com.example.event.service.impl;

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
import com.example.event.model.entity.Event;
import com.example.event.model.enums.EventType;
import com.example.event.repository.EventRepository;
import com.example.event.service.EventService;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);
    private static final int MEMBERS_PAGE_SIZE = 100;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' HH:mm");

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final MemberClient memberClient;
    private final NotificationClient notificationClient;

    public EventServiceImpl(EventRepository eventRepository, EventMapper eventMapper,
                            MemberClient memberClient, NotificationClient notificationClient) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.memberClient = memberClient;
        this.notificationClient = notificationClient;
    }

    @Override
    public PageResponse<EventResponse> findAll(EventType type, Pageable pageable) {
        return PageResponse.from(eventRepository.search(type, pageable), eventMapper::toResponse);
    }

    @Override
    public EventResponse findById(Long id) {
        return eventMapper.toResponse(getEventOrThrow(id));
    }

    @Override
    @Transactional
    public EventResponse create(EventRequest request) {
        return eventMapper.toResponse(eventRepository.save(eventMapper.toEntity(request)));
    }

    @Override
    @Transactional
    public EventResponse update(Long id, EventRequest request) {
        Event event = getEventOrThrow(id);
        eventMapper.updateEntity(request, event);
        return eventMapper.toResponse(eventRepository.saveAndFlush(event));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        eventRepository.delete(getEventOrThrow(id));
    }

    @Override
    public AnnouncementResponse announce(Long id) {
        Event event = getEventOrThrow(id);
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new InvalidOperationException("Event " + id + " is already over");
        }
        List<MemberContact> members = allMembers();
        String message = announcement(event);
        int sent = 0;
        for (MemberContact member : members) {
            try {
                notificationClient.create(new NotificationRequest(member.email(), message,
                        NotificationRequest.EVENT_ANNOUNCEMENT));
                sent++;
            } catch (FeignException ex) {
                log.warn("Could not notify {} about event {}: {}", member.email(), id, ex.getMessage());
            }
        }
        return new AnnouncementResponse(id, event.getTitle(), members.size(), sent, members.size() - sent);
    }

    private List<MemberContact> allMembers() {
        List<MemberContact> members = new ArrayList<>();
        PageResponse<MemberContact> page;
        int number = 0;
        do {
            page = memberClient.findAll(number++, MEMBERS_PAGE_SIZE);
            members.addAll(page.content());
        } while (!page.last());
        return members;
    }

    private static String announcement(Event event) {
        String type = event.getType().name().toLowerCase().replace('_', ' ');
        return "New " + type + " at the library: \"" + event.getTitle() + "\" on "
                + event.getEventDate().format(DATE_FORMAT) + ", " + event.getLocation() + ".";
    }

    private Event getEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event " + id + " not found"));
    }
}
