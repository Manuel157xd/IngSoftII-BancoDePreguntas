package com.taller2.presentacion.controller;

import com.taller2.negocio.model.Usuario;
import com.taller2.negocio.service.NotificationGateway;

import java.util.List;

public class NotificationController {
    private final NotificationGateway notificationGateway;

    public NotificationController(NotificationGateway notificationGateway) {
        this.notificationGateway = notificationGateway;
    }

    public List<NotificationGateway.InboxNotification> listForUser(Usuario user) {
        return notificationGateway.listForUser(user);
    }

    public void markAsRead(int notificationId, Usuario user) {
        notificationGateway.markAsRead(notificationId, user);
    }
}
