package com.example.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of announcing an event to every member")
public record AnnouncementResponse(
        Long eventId,
        String eventTitle,
        int recipients,
        int sent,
        int failed
) {
}
