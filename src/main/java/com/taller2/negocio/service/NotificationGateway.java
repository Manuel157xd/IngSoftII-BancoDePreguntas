package com.taller2.negocio.service;

import com.taller2.negocio.model.Usuario;

import java.util.List;

public interface NotificationGateway {
    List<InboxNotification> listForUser(Usuario user);

    void markAsRead(int notificationId, Usuario user);

    record InboxNotification(int id, String subject, String body, String createdAt, boolean read) {}
}
