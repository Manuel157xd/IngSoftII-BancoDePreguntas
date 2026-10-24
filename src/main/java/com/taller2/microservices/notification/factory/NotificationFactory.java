package com.taller2.microservices.notification.factory;

import com.taller2.microservices.events.model.DomainEvent;

public abstract class NotificationFactory {

    public record NotificationMessage(String recipient, String subject, String body) {}

    public abstract boolean supports(DomainEvent event);

    public final NotificationMessage create(DomainEvent event, String recipient) {
        if (!supports(event)) {
            throw new IllegalArgumentException("Esta fábrica no soporta el evento solicitado.");
        }
        return createMessage(event, recipient);
    }

    protected abstract NotificationMessage createMessage(DomainEvent event, String recipient);
}
