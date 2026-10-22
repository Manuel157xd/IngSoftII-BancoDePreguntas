package com.taller2.microservices.events.model;

import java.time.LocalDateTime;

/**
 * Evento base para la Arquitectura Orientada a Eventos (EDA) entre microservicios.
 */
public abstract class DomainEvent {
    private final String eventId;
    private final LocalDateTime timestamp;
    private final String eventType;

    protected DomainEvent(String eventType) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.eventType = eventType;
    }

    public String getEventId() { return eventId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getEventType() { return eventType; }
}
