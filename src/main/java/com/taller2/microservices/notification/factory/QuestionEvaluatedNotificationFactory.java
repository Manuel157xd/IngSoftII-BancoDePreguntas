package com.taller2.microservices.notification.factory;

import com.taller2.microservices.events.model.DomainEvent;
import com.taller2.microservices.events.model.QuestionEvaluatedEvent;
import org.springframework.stereotype.Component;

@Component
public class QuestionEvaluatedNotificationFactory extends NotificationFactory {

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof QuestionEvaluatedEvent;
    }

    @Override
    protected NotificationMessage createMessage(DomainEvent event, String recipient) {
        QuestionEvaluatedEvent evaluated = (QuestionEvaluatedEvent) event;
        String subject = "Resultado de Evaluación - Pregunta #" + evaluated.getQuestionId()
                + " [" + evaluated.getOutcome() + "]";
        String body = """
                Estimado docente autor,

                Su pregunta '%s' (ID #%d) ha sido evaluada por el revisor <%s>.
                Estado resultante: %s
                Observaciones recibidas:
                "%s"

                Atentamente,
                Comité Evaluador Banco Saber Pro
                """.formatted(
                evaluated.getQuestionName(), evaluated.getQuestionId(), evaluated.getReviewerEmail(),
                evaluated.getOutcome(), evaluated.getObservation()
        );
        return new NotificationMessage(recipient, subject, body);
    }
}
