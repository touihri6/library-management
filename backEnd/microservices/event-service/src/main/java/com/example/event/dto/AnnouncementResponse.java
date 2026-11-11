package com.example.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of announcing an event to every member")
public record AnnouncementResponse(
        @Schema(description = "Id of the announced event", example = "1")
        Long eventId,

        @Schema(description = "Title of the announced event", example = "Club de lecture : Dune")
        String eventTitle,

        @Schema(description = "Number of members read from member-service", example = "12")
        int recipients,

        @Schema(description = "Number of notifications created in notification-service", example = "11")
        int sent,

        @Schema(description = "Number of notifications that notification-service could not create", example = "1")
        int failed
) {
}
