package com.taller2.microservices.events.publisher;

import java.util.List;

public record EventEnvelope(
        String eventId,
        String eventType,
        int questionId,
        String questionName,
        List<String> reviewerEmails,
        String reviewerEmail,
        String outcome,
        String observation,
        Integer authorId
) {}
