package com.example.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A notification sent to notification-service through OpenFeign")
public record NotificationRequest(
        String recipient,
        String message,
        String type
) {

    public static final String EVENT_ANNOUNCEMENT = "EVENT_ANNOUNCEMENT";
}
