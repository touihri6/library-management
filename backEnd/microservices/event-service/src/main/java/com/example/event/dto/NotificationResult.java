package com.example.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A notification created by notification-service")
public record NotificationResult(
        @Schema(description = "Technical id of the notification in notification-service", example = "42")
        Long id,

        @Schema(description = "Email address of the recipient", example = "nour@mail.com")
        String recipient
) {
}
