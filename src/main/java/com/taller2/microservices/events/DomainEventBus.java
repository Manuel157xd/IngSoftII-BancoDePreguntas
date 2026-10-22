package com.taller2.microservices.events;

import com.taller2.microservices.events.model.DomainEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Patrón GoF de Comportamiento: Observer
 * Bus de eventos de dominio desacoplado para la Arquitectura Orientada a Eventos (EDA).
 * Permite a los microservicios suscribirse y reaccionar asíncronamente a cambios en otros servicios.
 */
@Component
public class DomainEventBus {

    private final List<Consumer<DomainEvent>> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(Consumer<DomainEvent> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

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
