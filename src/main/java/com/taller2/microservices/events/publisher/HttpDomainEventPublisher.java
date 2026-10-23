package com.taller2.microservices.events.publisher;

import com.taller2.microservices.events.model.DomainEvent;
import com.taller2.microservices.events.model.QuestionEvaluatedEvent;
import com.taller2.microservices.events.model.QuestionSubmittedEvent;
import com.taller2.microservices.events.model.ReviewersAssignedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@Profile("distributed")
public class HttpDomainEventPublisher implements DomainEventPublisher {

    private final RestClient restClient;

    public HttpDomainEventPublisher(
            RestClient.Builder builder,
            @Value("${services.notifications.base-url:http://localhost:8083}") String notificationBaseUrl
    ) {
        this.restClient = builder.baseUrl(notificationBaseUrl).build();
    }

    @Override
    public void publish(DomainEvent event) {
        EventEnvelope envelope;
        if (event instanceof ReviewersAssignedEvent assigned) {
            envelope = new EventEnvelope(
                    event.getEventId(), event.getEventType(), assigned.getQuestionId(),
                    assigned.getQuestionName(), assigned.getReviewerEmails(), null, null, null, null
            );
        } else if (event instanceof QuestionEvaluatedEvent evaluated) {
            envelope = new EventEnvelope(
                    event.getEventId(), event.getEventType(), evaluated.getQuestionId(),
                    evaluated.getQuestionName(), List.of(), evaluated.getReviewerEmail(),
                    evaluated.getOutcome(), evaluated.getObservation(), evaluated.getAuthorId()
            );
        } else if (event instanceof QuestionSubmittedEvent submitted) {
            envelope = new EventEnvelope(
                    event.getEventId(), event.getEventType(), submitted.getQuestionId(),
                    submitted.getQuestionName(), List.of(), null, null, null, submitted.getAuthorId()
            );
        } else {
            throw new IllegalArgumentException("Tipo de evento no soportado: " + event.getClass().getName());
        }

        restClient.post()
                .uri("/api/notifications/events")
                .body(envelope)
                .retrieve()
                .toBodilessEntity();
    }
}
