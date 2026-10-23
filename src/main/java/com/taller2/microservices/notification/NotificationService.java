package com.taller2.microservices.notification;

import com.taller2.microservices.events.model.DomainEvent;
import com.taller2.microservices.events.model.QuestionEvaluatedEvent;
import com.taller2.microservices.events.model.ReviewersAssignedEvent;
import com.taller2.microservices.events.publisher.EventEnvelope;
import com.taller2.microservices.notification.adapter.EmailSenderAdapter;
import com.taller2.microservices.notification.factory.NotificationFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final EmailSenderAdapter emailSender;

    public NotificationService(EmailSenderAdapter emailSender) {
        this.emailSender = emailSender;
    }

    public void handleEvent(DomainEvent event) {
        if (event instanceof ReviewersAssignedEvent assignedEvent) {
            for (String email : assignedEvent.getReviewerEmails()) {
                NotificationFactory.NotificationMessage message =
                        NotificationFactory.createForReviewersAssigned(assignedEvent, email);
                emailSender.sendEmail(message.recipient(), message.subject(), message.body());
            }
        } else if (event instanceof QuestionEvaluatedEvent evaluatedEvent) {
            String authorEmail = evaluatedEvent.getAuthorId() != null
                    ? "autor" + evaluatedEvent.getAuthorId() + "@unicauca.edu.co"
                    : "autor.desconocido@unicauca.edu.co";
            NotificationFactory.NotificationMessage message =
                    NotificationFactory.createForQuestionEvaluated(evaluatedEvent, authorEmail);
            emailSender.sendEmail(message.recipient(), message.subject(), message.body());
        }
    }

    public void handleEvent(EventEnvelope envelope) {
        switch (envelope.eventType()) {
            case "REVIEWERS_ASSIGNED" -> handleEvent(new ReviewersAssignedEvent(
                    envelope.questionId(), envelope.questionName(), envelope.reviewerEmails()
            ));
            case "QUESTION_EVALUATED" -> handleEvent(new QuestionEvaluatedEvent(
                    envelope.questionId(), envelope.questionName(), envelope.reviewerEmail(),
                    envelope.outcome(), envelope.observation(), envelope.authorId()
            ));
            case "QUESTION_SUBMITTED_FOR_REVIEW" -> {
                // This event is available for future notification workflows.
            }
            default -> throw new IllegalArgumentException(
                    "Tipo de evento no soportado: " + envelope.eventType()
            );
        }
    }
}
