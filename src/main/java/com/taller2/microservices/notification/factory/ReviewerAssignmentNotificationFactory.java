package com.taller2.microservices.notification.factory;

import com.taller2.microservices.events.model.DomainEvent;
import com.taller2.microservices.events.model.ReviewersAssignedEvent;
import org.springframework.stereotype.Component;

@Component
public class ReviewerAssignmentNotificationFactory extends NotificationFactory {

    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof ReviewersAssignedEvent;
    }

    @Override
    protected NotificationMessage createMessage(DomainEvent event, String recipient) {
        ReviewersAssignedEvent assigned = (ReviewersAssignedEvent) event;
        String subject = "Asignación de Revisión - Pregunta #" + assigned.getQuestionId();
        String body = """
                Estimado docente revisor,

                Se le ha asignado para evaluar la pregunta: '%s' (ID #%d).
                Por favor ingrese al sistema para validar los contenidos y emitir su concepto.

                Atentamente,
                Administración Banco Saber Pro
                """.formatted(assigned.getQuestionName(), assigned.getQuestionId());
        return new NotificationMessage(recipient, subject, body);
    }
}
