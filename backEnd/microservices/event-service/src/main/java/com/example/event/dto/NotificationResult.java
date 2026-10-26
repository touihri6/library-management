package com.example.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A notification created by notification-service")
public record NotificationResult(
        Long id,
        String recipient
) {
}
