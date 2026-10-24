package com.taller2.microservices.notification;

public record NotificationRecord(
        int id,
        String subject,
        String body,
        String createdAt,
        boolean read
) {}
