package com.taller2.microservices.notification;

import com.taller2.microservices.events.model.DomainEvent;
import com.taller2.microservices.events.model.QuestionEvaluatedEvent;
import com.taller2.microservices.events.model.ReviewersAssignedEvent;
import com.taller2.microservices.events.publisher.EventEnvelope;
import com.taller2.microservices.notification.adapter.EmailSenderAdapter;
import com.taller2.microservices.notification.factory.NotificationFactory;
import com.taller2.microservices.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final EmailSenderAdapter emailSender;
    private final NotificationRepository repository;
    private final List<NotificationFactory> notificationFactories;

    public NotificationService(
            EmailSenderAdapter emailSender,
            NotificationRepository repository,
            List<NotificationFactory> notificationFactories
    ) {
        this.emailSender = emailSender;
        this.repository = repository;
        this.notificationFactories = List.copyOf(notificationFactories);
    }

    public void handleEvent(DomainEvent event) {
        if (event instanceof ReviewersAssignedEvent assignedEvent) {
            validateReviewerEmails(assignedEvent.getReviewerEmails());
            for (String email : assignedEvent.getReviewerEmails()) {
                String recipient = normalizeEmail(email);
                notifyRecipient(event.getEventId(), recipient, createMessage(assignedEvent, recipient));
            }
        } else if (event instanceof QuestionEvaluatedEvent evaluatedEvent) {
            if (evaluatedEvent.getAuthorId() == null) {
                return;
            }
            String recipient = userRecipient(evaluatedEvent.getAuthorId());
            notifyRecipient(event.getEventId(), recipient, createMessage(evaluatedEvent, recipient));
        }
    }

    public void handleEvent(EventEnvelope envelope) {
        switch (envelope.eventType()) {
            case "REVIEWERS_ASSIGNED" -> {
                ReviewersAssignedEvent event = new ReviewersAssignedEvent(
                        envelope.questionId(), envelope.questionName(), envelope.reviewerEmails()
                );
                validateReviewerEmails(event.getReviewerEmails());
                for (String email : event.getReviewerEmails()) {
                    String recipient = normalizeEmail(email);
                    notifyRecipient(envelope.eventId(), recipient, createMessage(event, recipient));
                }
            }
            case "QUESTION_EVALUATED" -> {
                if (envelope.authorId() == null) {
                    return;
                }
                String recipient = userRecipient(envelope.authorId());
                QuestionEvaluatedEvent event = new QuestionEvaluatedEvent(
                        envelope.questionId(), envelope.questionName(), envelope.reviewerEmail(),
                        envelope.outcome(), envelope.observation(), envelope.authorId()
                );
                notifyRecipient(envelope.eventId(), recipient, createMessage(event, recipient));
            }
            case "QUESTION_SUBMITTED_FOR_REVIEW" -> {
                // This event is available for future notification workflows.
            }
            default -> throw new IllegalArgumentException(
                    "Tipo de evento no soportado: " + envelope.eventType()
            );
        }
    }

    private void notifyRecipient(
            String eventId,
            String recipient,
            NotificationFactory.NotificationMessage message
    ) {
        if (repository.save(eventId, recipient, message.subject(), message.body())) {
            emailSender.sendEmail(recipient, message.subject(), message.body());
        }
    }

    private NotificationFactory.NotificationMessage createMessage(DomainEvent event, String recipient) {
        return notificationFactories.stream()
                .filter(factory -> factory.supports(event))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No hay una fábrica de notificaciones para el evento " + event.getEventType()
                ))
                .create(event, recipient);
    }

    public static String userRecipient(int userId) {
        return "USER_ID:" + userId;
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static void validateReviewerEmails(java.util.List<String> emails) {
        if (emails == null || emails.isEmpty() || emails.size() > 3
                || emails.stream().anyMatch(email -> email == null || !email.contains("@"))) {
            throw new IllegalArgumentException("El evento debe incluir entre 1 y 3 correos de revisor válidos.");
        }
    }
}
