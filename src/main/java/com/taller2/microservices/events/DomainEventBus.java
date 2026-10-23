package com.taller2.microservices.events;

import com.taller2.microservices.events.model.DomainEvent;
import com.taller2.microservices.events.publisher.DomainEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Patrón GoF de Comportamiento: Observer
 * Bus local del modo combinado. En el modo distribuido, los eventos se envían por HTTP.
 */
@Component
@Profile("!distributed")
public class DomainEventBus implements DomainEventPublisher {

    private final List<Consumer<DomainEvent>> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(Consumer<DomainEvent> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @Override
    public void publish(DomainEvent event) {
        if (event == null) return;
        for (Consumer<DomainEvent> listener : listeners) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                System.err.println("[DomainEventBus] Error procesando evento: " + e.getMessage());
            }
        }
    }
}
