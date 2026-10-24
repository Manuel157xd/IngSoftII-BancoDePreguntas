package com.taller2.microservices.notification;

import com.taller2.microservices.events.publisher.EventEnvelope;
import com.taller2.microservices.notification.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationEventController {

    private final NotificationService notificationService;
    private final NotificationRepository repository;

    public NotificationEventController(
            NotificationService notificationService,
            NotificationRepository repository
    ) {
        this.notificationService = notificationService;
        this.repository = repository;
    }

    public record ReadNotificationRequest(String recipient) {}

    @PostMapping("/events")
    public ResponseEntity<?> receiveEvent(@RequestBody EventEnvelope envelope) {
        if (envelope == null || envelope.eventId() == null || envelope.eventId().isBlank()
                || envelope.eventType() == null || envelope.eventType().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El evento requiere id y tipo"));
        }
        try {
            notificationService.handleEvent(envelope);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        }
        return ResponseEntity.ok(Map.of(
                "eventId", envelope.eventId(),
                "status", "processed"
        ));
    }

    @GetMapping("/inbox")
    public ResponseEntity<?> getInbox(@RequestParam String recipient) {
        if (recipient == null || recipient.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El destinatario es obligatorio"));
        }
        return ResponseEntity.ok(repository.findByRecipient(normalizeRecipient(recipient)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markRead(
            @PathVariable int id,
            @RequestBody ReadNotificationRequest request
    ) {
        if (request == null || request.recipient() == null || request.recipient().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El destinatario es obligatorio"));
        }
        if (!repository.markRead(id, normalizeRecipient(request.recipient()))) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    private String normalizeRecipient(String recipient) {
        String trimmed = recipient.trim();
        return trimmed.regionMatches(true, 0, "USER_ID:", 0, "USER_ID:".length())
                ? "USER_ID:" + trimmed.substring("USER_ID:".length())
                : trimmed.toLowerCase(java.util.Locale.ROOT);
    }
}
