package com.example.event.dto;

import com.example.event.model.enums.EventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;

@Schema(description = "An event as returned by the API")
public record EventResponse(
        Long id,
        String title,
        String description,
        EventType type,
        LocalDateTime eventDate,
        String location,
        Integer capacity,
        Instant createdAt,
        Instant updatedAt
) {
}
