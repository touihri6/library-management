package com.example.event.dto;

import com.example.event.model.enums.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Payload used to create or update an event")
public record EventRequest(

        @Schema(description = "Title of the event", example = "Club de lecture : Dune")
        @NotBlank @Size(max = 150)
        String title,

        @Schema(description = "Description of the event, at most 1000 characters", example = "Discussion autour du roman de Frank Herbert", nullable = true)
        @Size(max = 1000)
        String description,

        @Schema(description = "Type of event", example = "READING_CLUB")
        @NotNull
        EventType type,

        @Schema(description = "Local date and time of the event; must be in the future", example = "2027-01-15T18:00:00")
        @NotNull @Future
        LocalDateTime eventDate,

        @Schema(description = "Room or place where the event takes place", example = "Salle A")
        @NotBlank @Size(max = 120)
        String location,

        @Schema(description = "Maximum number of attendees, greater than 0", example = "25")
        @NotNull @Positive
        Integer capacity
) {
}
