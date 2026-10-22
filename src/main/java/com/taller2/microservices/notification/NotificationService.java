package com.taller2.microservices.notification;

import com.taller2.microservices.events.DomainEventBus;
import com.taller2.microservices.events.model.DomainEvent;
import com.taller2.microservices.events.model.QuestionEvaluatedEvent;
import com.taller2.microservices.events.model.ReviewersAssignedEvent;
import com.taller2.microservices.notification.adapter.EmailSenderAdapter;
import com.taller2.microservices.notification.factory.NotificationFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

/**
 * Microservicio de Notificaciones.
 * Escucha eventos del DomainEventBus y despacha correos según las historias de usuario HU4 y HU5.
 */
@Service
public class NotificationService {

    private final DomainEventBus eventBus;
    private final EmailSenderAdapter emailSender;

    public NotificationService(DomainEventBus eventBus, EmailSenderAdapter emailSender) {
        this.eventBus = eventBus;
        this.emailSender = emailSender;
    }

    @PostConstruct
    public void init() {
        eventBus.subscribe(this::handleEvent);
    }

    public void handleEvent(DomainEvent event) {
        if (event instanceof ReviewersAssignedEvent revEvent) {
            // Requisito HU4: Notificar a los revisores asignados
            for (String email : revEvent.getReviewerEmails()) {
                NotificationFactory.NotificationMessage msg =
                        NotificationFactory.createForReviewersAssigned(revEvent, email);
                emailSender.sendEmail(msg.recipient(), msg.subject(), msg.body());
            }
        } else if (event instanceof QuestionEvaluatedEvent evalEvent) {
            // Requisito HU5: Notificar al autor sobre observaciones y cambio de estado
            String authorEmail = evalEvent.getAuthorId() != null
                    ? "autor" + evalEvent.getAuthorId() + "@unicauca.edu.co"
                    : "autor.desconocido@unicauca.edu.co";
            NotificationFactory.NotificationMessage msg =
                    NotificationFactory.createForQuestionEvaluated(evalEvent, authorEmail);
            emailSender.sendEmail(msg.recipient(), msg.subject(), msg.body());
        }
    }
}
