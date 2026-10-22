package com.taller2.microservices.notification.adapter;

/**
 * Patrón Estructural GoF: Adapter
 * Desacopla la lógica de envío de emails del proveedor real (JavaMail, SendGrid, Consola).
 */
public interface EmailSenderAdapter {
    void sendEmail(String to, String subject, String body);
}
