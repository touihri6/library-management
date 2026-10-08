package com.example.event.controller;

import com.example.event.dto.EventRequest;
import com.example.event.dto.EventResponse;
import com.example.event.dto.PageResponse;
import com.example.event.model.enums.EventType;
import com.example.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events", description = "Library events: reading clubs, workshops, conferences, book signings")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "List events", description = "Paginated list sorted by date, optionally filtered by type")
    public PageResponse<EventResponse> findAll(
            @Parameter(description = "Event type") @RequestParam(required = false) EventType type,
            @ParameterObject @PageableDefault(size = 20, sort = "eventDate", direction = Sort.Direction.ASC) Pageable pageable) {
        return eventService.findAll(type, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an event by id")
    @ApiResponse(responseCode = "200", description = "Event found")
    @ApiResponse(responseCode = "404", description = "Event not found")
    public EventResponse findById(@PathVariable Long id) {
        return eventService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create an event")
    @ApiResponse(responseCode = "201", description = "Event created")
    @ApiResponse(responseCode = "400", description = "Invalid payload or date in the past")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request) {
        EventResponse created = eventService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an event (full replacement)")
    @ApiResponse(responseCode = "200", description = "Event updated")
    @ApiResponse(responseCode = "400", description = "Invalid payload or date in the past")
    @ApiResponse(responseCode = "404", description = "Event not found")
    public EventResponse update(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return eventService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an event")
    @ApiResponse(responseCode = "204", description = "Event deleted")
    @ApiResponse(responseCode = "404", description = "Event not found")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
