package com.example.event.client;

import com.example.event.dto.NotificationRequest;
import com.example.event.dto.NotificationResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationClient {

    @PostMapping("/api/v1/notifications")
    NotificationResult create(@RequestBody NotificationRequest request);
}
