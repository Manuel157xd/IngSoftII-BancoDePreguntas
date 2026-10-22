package com.taller2.microservices.notification.adapter;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class LogEmailSenderAdapter implements EmailSenderAdapter {

    public record SentEmail(String to, String subject, String body, LocalDateTime sentAt) {}

    private final List<SentEmail> sentEmails = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void sendEmail(String to, String subject, String body) {
        SentEmail email = new SentEmail(to, subject, body, LocalDateTime.now());
        sentEmails.add(email);
        System.out.printf("[EmailService - Adapter] ✉️ Correo enviado a <%s> | Asunto: %s%n", to, subject);
        System.out.printf("[EmailService - Contenido]%n%s%n", body);
    }

    public List<SentEmail> getSentEmails() {
        return List.copyOf(sentEmails);
    }
}
