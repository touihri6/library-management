package com.example.event.service.impl;

import com.example.event.dto.EventRequest;
import com.example.event.dto.EventResponse;
import com.example.event.dto.PageResponse;
import com.example.event.exception.ResourceNotFoundException;
import com.example.event.mapper.EventMapper;
import com.example.event.model.entity.Event;
import com.example.event.model.enums.EventType;
import com.example.event.repository.EventRepository;
import com.example.event.service.EventService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public EventServiceImpl(EventRepository eventRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
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

    private Event getEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event " + id + " not found"));
    }
}
