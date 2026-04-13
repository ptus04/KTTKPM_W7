package com.sba.notification.controller;

import com.sba.common.dto.NotificationRequest;
import com.sba.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/notifications")
    public ResponseEntity<Void> notify(@Valid @RequestBody NotificationRequest request) {
        notificationService.notify(request);
        return ResponseEntity.ok().build();
    }
}
