package com.taller2.microservices.notification;

import com.taller2.microservices.events.model.DomainEvent;
import com.taller2.microservices.events.model.QuestionEvaluatedEvent;
import com.taller2.microservices.events.model.ReviewersAssignedEvent;
import com.taller2.microservices.events.publisher.EventEnvelope;
import com.taller2.microservices.notification.adapter.EmailSenderAdapter;
import com.taller2.microservices.notification.factory.NotificationFactory;
import com.taller2.microservices.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final EmailSenderAdapter emailSender;
    private final NotificationRepository repository;

    public NotificationService(EmailSenderAdapter emailSender, NotificationRepository repository) {
        this.emailSender = emailSender;
        this.repository = repository;
    }

    public void handleEvent(DomainEvent event) {
        if (event instanceof ReviewersAssignedEvent assignedEvent) {
            validateReviewerEmails(assignedEvent.getReviewerEmails());
            for (String email : assignedEvent.getReviewerEmails()) {
                NotificationFactory.NotificationMessage message =
                        NotificationFactory.createForReviewersAssigned(assignedEvent, email);
                notifyRecipient(event.getEventId(), normalizeEmail(message.recipient()), message);
            }
        } else if (event instanceof QuestionEvaluatedEvent evaluatedEvent) {
            if (evaluatedEvent.getAuthorId() == null) {
                return;
            }
            String recipient = userRecipient(evaluatedEvent.getAuthorId());
            String authorEmail = "autor" + evaluatedEvent.getAuthorId() + "@unicauca.edu.co";
            NotificationFactory.NotificationMessage message =
                    NotificationFactory.createForQuestionEvaluated(evaluatedEvent, authorEmail);
            notifyRecipient(event.getEventId(), recipient, message);
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
                    NotificationFactory.NotificationMessage message =
                            NotificationFactory.createForReviewersAssigned(event, email);
                    notifyRecipient(envelope.eventId(), normalizeEmail(email), message);
                }
            }
            case "QUESTION_EVALUATED" -> {
                if (envelope.authorId() == null) {
                    return;
                }
                String recipient = userRecipient(envelope.authorId());
                String authorEmail = "autor" + envelope.authorId() + "@unicauca.edu.co";
                QuestionEvaluatedEvent event = new QuestionEvaluatedEvent(
                        envelope.questionId(), envelope.questionName(), envelope.reviewerEmail(),
                        envelope.outcome(), envelope.observation(), envelope.authorId()
                );
                NotificationFactory.NotificationMessage message =
                        NotificationFactory.createForQuestionEvaluated(event, authorEmail);
                notifyRecipient(envelope.eventId(), recipient, message);
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
            emailSender.sendEmail(message.recipient(), message.subject(), message.body());
        }
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
