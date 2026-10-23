package com.taller2.microservices.notification;

import com.taller2.microservices.events.publisher.EventEnvelope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationEventController {

    private final NotificationService notificationService;

    public NotificationEventController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/events")
    public ResponseEntity<?> receiveEvent(@RequestBody EventEnvelope envelope) {
        if (envelope.eventId() == null || envelope.eventType() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El evento requiere id y tipo"));
        }
        notificationService.handleEvent(envelope);
        return ResponseEntity.ok(Map.of(
                "eventId", envelope.eventId(),
                "status", "processed"
        ));
    }
}
