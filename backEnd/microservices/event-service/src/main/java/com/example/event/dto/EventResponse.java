package com.example.event.dto;

import com.example.event.model.enums.EventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;

@Schema(description = "An event as returned by the API")
public record EventResponse(
        @Schema(description = "Technical id of the event", example = "1")
        Long id,

        @Schema(description = "Title of the event", example = "Club de lecture : Dune")
        String title,

        @Schema(description = "Description of the event", example = "Discussion autour du roman de Frank Herbert", nullable = true)
        String description,

        @Schema(description = "Type of event", example = "READING_CLUB")
        EventType type,

        @Schema(description = "Local date and time of the event", example = "2027-01-15T18:00:00")
        LocalDateTime eventDate,

        @Schema(description = "Room or place where the event takes place", example = "Salle A")
        String location,

        @Schema(description = "Maximum number of attendees", example = "25")
        Integer capacity,

        @Schema(description = "When the event was created (UTC)", example = "2026-10-01T09:00:00Z")
        Instant createdAt,

        @Schema(description = "When the event was last modified (UTC)", example = "2026-10-02T11:30:00Z")
        Instant updatedAt,

        @Schema(description = "Keycloak id (sub) of the user who created the event", example = "5f0c2a9e-3b7d-4f1e-9a51-1c2d3e4f5a6b", nullable = true)
        String createdBy,

        @Schema(description = "Keycloak id (sub) of the user who last modified the event", example = "5f0c2a9e-3b7d-4f1e-9a51-1c2d3e4f5a6b", nullable = true)
        String updatedBy
) {
}
