package com.taller2.microservices.runtime;

import com.taller2.microservices.MicroservicesApplication;
import org.springframework.boot.SpringApplication;

import java.util.Arrays;

public final class ServiceLauncherApplication {

    private ServiceLauncherApplication() {}

    public static void main(String[] args) {
        String service = Arrays.stream(args)
                .filter(argument -> argument.startsWith("--app.service="))
                .map(argument -> argument.substring("--app.service=".length()))
                .findFirst()
                .orElse("combined");
        String[] applicationArgs = Arrays.stream(args)
                .filter(argument -> !argument.startsWith("--app.service="))
                .toArray(String[]::new);

        if ("combined".equals(service)) {
            MicroservicesApplication.main(applicationArgs);
            return;
        }
        if ("desktop".equals(service)) {
            com.taller2.Main.main(applicationArgs);
            return;
        }

        Class<?> application = switch (service) {
            case "questions" -> QuestionsServiceApplication.class;
            case "reviews" -> ReviewServiceApplication.class;
            case "notifications" -> NotificationServiceApplication.class;
            case "identity" -> IdentityServiceApplication.class;
            default -> throw new IllegalArgumentException(
                    "Servicio no reconocido: " + service
                            + ". Valores: questions, reviews, notifications, identity, combined, desktop"
            );
        };
        SpringApplication.run(application, applicationArgs);
    }
}
