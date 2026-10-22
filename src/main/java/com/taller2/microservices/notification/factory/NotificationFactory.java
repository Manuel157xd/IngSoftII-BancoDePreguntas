package com.taller2.microservices.notification.factory;

import com.taller2.microservices.events.model.DomainEvent;
import com.taller2.microservices.events.model.QuestionEvaluatedEvent;
import com.taller2.microservices.events.model.ReviewersAssignedEvent;

/**
 * Patrón Creacional GoF: Factory Method
 * Fabrica el contenido de la notificación según el evento de dominio recibido.
 */
public class NotificationFactory {

    public record NotificationMessage(String recipient, String subject, String body) {}

    public static NotificationMessage createForReviewersAssigned(ReviewersAssignedEvent event, String recipientEmail) {
        String subject = "Asignación de Revisión - Pregunta #" + event.getQuestionId();
        String body = String.format(
                "Estimado docente revisor,%n%nSe le ha asignado para evaluar la pregunta: '%s' (ID #%d).%n" +
                "Por favor ingrese al sistema para validar los contenidos y emitir su concepto.%n%nAtentamente,%nAdministración Banco Saber Pro",
                event.getQuestionName(), event.getQuestionId()
        );
        return new NotificationMessage(recipientEmail, subject, body);
    }

    public static NotificationMessage createForQuestionEvaluated(QuestionEvaluatedEvent event, String authorEmail) {
        String subject = "Resultado de Evaluación - Pregunta #" + event.getQuestionId() + " [" + event.getOutcome() + "]";
        String body = String.format(
                "Estimado docente autor,%n%nSu pregunta '%s' (ID #%d) ha sido evaluada por el revisor <%s>.%n" +
                "Estado resultante: %s%n" +
                "Observaciones recibidas:%n\"%s\"%n%nAtentamente,%nComité Evaluador Banco Saber Pro",
                event.getQuestionName(), event.getQuestionId(), event.getReviewerEmail(),
                event.getOutcome(), event.getObservation()
        );
        return new NotificationMessage(authorEmail, subject, body);
    }
}
