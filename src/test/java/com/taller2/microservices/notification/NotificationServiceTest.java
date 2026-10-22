package com.taller2.microservices.notification;

import com.taller2.microservices.events.DomainEventBus;
import com.taller2.microservices.events.model.QuestionEvaluatedEvent;
import com.taller2.microservices.events.model.ReviewersAssignedEvent;
import com.taller2.microservices.notification.adapter.LogEmailSenderAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private DomainEventBus eventBus;
    private LogEmailSenderAdapter emailAdapter;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        eventBus = new DomainEventBus();
        emailAdapter = new LogEmailSenderAdapter();
        notificationService = new NotificationService(eventBus, emailAdapter);
        notificationService.init();
    }

    @Test
    void testNotificacionAsignacionRevisoresDisparaEmails() {
        // HU4: Al asignar 2 revisores, se envían 2 correos
        ReviewersAssignedEvent event = new ReviewersAssignedEvent(
                42,
                "Pregunta Arquitectura Microservicios",
                List.of("revisor1@unicauca.edu.co", "revisor2@unicauca.edu.co")
        );

        eventBus.publish(event);

        List<LogEmailSenderAdapter.SentEmail> sent = emailAdapter.getSentEmails();
        assertEquals(2, sent.size());
        assertTrue(sent.stream().anyMatch(e -> e.to().equals("revisor1@unicauca.edu.co")));
        assertTrue(sent.stream().anyMatch(e -> e.to().equals("revisor2@unicauca.edu.co")));
        assertTrue(sent.get(0).subject().contains("Asignación de Revisión"));
    }

    @Test
    void testNotificacionEvaluacionDisparaEmailAlAutor() {
        // HU5: Al evaluar y aprobar/rechazar, se notifica al autor
        QuestionEvaluatedEvent event = new QuestionEvaluatedEvent(
                42,
                "Pregunta Arquitectura Microservicios",
                "revisor1@unicauca.edu.co",
                "APROBADA",
                "Excelente formulación según la taxonomía.",
                5
        );

        eventBus.publish(event);

        List<LogEmailSenderAdapter.SentEmail> sent = emailAdapter.getSentEmails();
        assertEquals(1, sent.size());
        assertEquals("autor5@unicauca.edu.co", sent.get(0).to());
        assertTrue(sent.get(0).subject().contains("APROBADA"));
        assertTrue(sent.get(0).body().contains("Excelente formulación"));
    }
}
