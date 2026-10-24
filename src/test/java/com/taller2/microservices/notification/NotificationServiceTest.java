package com.taller2.microservices.notification;

import com.taller2.microservices.events.DomainEventBus;
import com.taller2.microservices.events.model.QuestionEvaluatedEvent;
import com.taller2.microservices.events.model.ReviewersAssignedEvent;
import com.taller2.microservices.events.publisher.EventEnvelope;
import com.taller2.microservices.notification.adapter.LogEmailSenderAdapter;
import com.taller2.microservices.notification.factory.QuestionEvaluatedNotificationFactory;
import com.taller2.microservices.notification.factory.ReviewerAssignmentNotificationFactory;
import com.taller2.microservices.notification.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private DomainEventBus eventBus;
    private LogEmailSenderAdapter emailAdapter;
    private NotificationService notificationService;
    private NotificationRepository repository;

    @BeforeEach
    void setUp() {
        new File("test_notifications.db").delete();
        repository = new NotificationRepository("jdbc:sqlite:test_notifications.db");
        eventBus = new DomainEventBus();
        emailAdapter = new LogEmailSenderAdapter();
        notificationService = new NotificationService(
                emailAdapter,
                repository,
                List.of(new ReviewerAssignmentNotificationFactory(), new QuestionEvaluatedNotificationFactory())
        );
        eventBus.subscribe(notificationService::handleEvent);
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
        assertEquals(1, repository.findByRecipient("revisor1@unicauca.edu.co").size());
        assertEquals(1, repository.findByRecipient("revisor2@unicauca.edu.co").size());
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
        assertEquals("USER_ID:5", sent.get(0).to());
        assertTrue(sent.get(0).subject().contains("APROBADA"));
        assertTrue(sent.get(0).body().contains("Excelente formulación"));
        assertEquals(1, repository.findByRecipient("USER_ID:5").size());
        assertTrue(repository.findByRecipient("USER_ID:5").get(0).body().contains("Excelente formulación"));
    }

    @Test
    void repeatedEventCreatesSingleInboxEntryAndNotificationCanBeMarkedRead() {
        ReviewersAssignedEvent event = new ReviewersAssignedEvent(
                42, "Pregunta de prueba", List.of("revisor@unicauca.edu.co")
        );

        notificationService.handleEvent(event);
        notificationService.handleEvent(event);

        var inbox = repository.findByRecipient("revisor@unicauca.edu.co");
        assertEquals(1, inbox.size());
        assertEquals(1, emailAdapter.getSentEmails().size());
        assertFalse(inbox.get(0).read());
        assertTrue(repository.markRead(inbox.get(0).id(), "revisor@unicauca.edu.co"));
        assertTrue(repository.findByRecipient("revisor@unicauca.edu.co").get(0).read());
        assertFalse(repository.markRead(inbox.get(0).id(), "otro@unicauca.edu.co"));
    }

    @Test
    void eventReceivedOverHttpIsStoredInInboxAndCanBeRead() {
        var controller = new NotificationEventController(notificationService, repository);
        EventEnvelope event = new EventEnvelope(
                "event-123", "REVIEWERS_ASSIGNED", 12, "Pregunta de integración",
                List.of("revisor@unicauca.edu.co"), null, null, null, null
        );

        assertEquals(HttpStatus.OK, controller.receiveEvent(event).getStatusCode());
        var inbox = controller.getInbox("REVISOR@unicauca.edu.co");
        assertEquals(HttpStatus.OK, inbox.getStatusCode());
        assertEquals(1, repository.findByRecipient("revisor@unicauca.edu.co").size());

        var notification = repository.findByRecipient("revisor@unicauca.edu.co").get(0);
        assertEquals(HttpStatus.NO_CONTENT,
                controller.markRead(notification.id(),
                        new NotificationEventController.ReadNotificationRequest("revisor@unicauca.edu.co"))
                        .getStatusCode());
        assertTrue(repository.findByRecipient("revisor@unicauca.edu.co").get(0).read());
    }
}
