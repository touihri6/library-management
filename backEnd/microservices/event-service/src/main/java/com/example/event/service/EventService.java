package com.example.event.service;

import com.example.event.dto.EventRequest;
import com.example.event.dto.EventResponse;
import com.example.event.dto.PageResponse;
import com.example.event.model.enums.EventType;
import org.springframework.data.domain.Pageable;

public interface EventService {

    PageResponse<EventResponse> findAll(EventType type, Pageable pageable);

    EventResponse findById(Long id);

    EventResponse create(EventRequest request);

    EventResponse update(Long id, EventRequest request);

    void delete(Long id);
}
