package com.taller2.persistencia.remote;

public record RemoteServiceUrls(String questions, String identity, String reviews, String notifications) {
    public static RemoteServiceUrls localDefaults() {
        return new RemoteServiceUrls(
                setting("banco.services.questions.url", "BANCO_QUESTIONS_URL", "http://localhost:8081"),
                setting("banco.services.identity.url", "BANCO_IDENTITY_URL", "http://localhost:8084"),
                setting("banco.services.reviews.url", "BANCO_REVIEWS_URL", "http://localhost:8082"),
                setting("banco.services.notifications.url", "BANCO_NOTIFICATIONS_URL", "http://localhost:8083")
        );
    }

    private static String setting(String property, String environment, String fallback) {
        String configured = System.getProperty(property);
        if (configured == null || configured.isBlank()) {
            configured = System.getenv(environment);
        }
        return configured == null || configured.isBlank() ? fallback : configured;
    }
}
