package com.example.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A notification sent to notification-service through OpenFeign")
public record NotificationRequest(
        @Schema(description = "Email address of the recipient", example = "nour@mail.com")
        String recipient,

        @Schema(description = "Text of the notification", example = "New reading club at the library: \"Club de lecture : Dune\" on 15/01/2027 at 18:00, Salle A.")
        String message,

        @Schema(description = "Notification type", example = "EVENT_ANNOUNCEMENT")
        String type
) {

    public static final String EVENT_ANNOUNCEMENT = "EVENT_ANNOUNCEMENT";
}
