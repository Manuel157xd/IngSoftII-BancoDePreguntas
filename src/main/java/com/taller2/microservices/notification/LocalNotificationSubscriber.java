package com.taller2.microservices.notification;

import com.taller2.microservices.events.DomainEventBus;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!distributed")
public class LocalNotificationSubscriber {

    private final DomainEventBus eventBus;
    private final NotificationService notificationService;

    public LocalNotificationSubscriber(DomainEventBus eventBus, NotificationService notificationService) {
        this.eventBus = eventBus;
        this.notificationService = notificationService;
    }

    @PostConstruct
    public void subscribe() {
        eventBus.subscribe(notificationService::handleEvent);
    }
}
