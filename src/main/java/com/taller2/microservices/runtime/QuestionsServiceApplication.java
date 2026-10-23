package com.taller2.microservices.runtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.taller2.microservices.questions",
        "com.taller2.microservices.validation",
        "com.taller2.microservices.events.publisher"
})
public class QuestionsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuestionsServiceApplication.class, args);
    }
}
