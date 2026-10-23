package com.taller2.microservices.runtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.taller2.microservices.review",
        "com.taller2.microservices.questionclient",
        "com.taller2.microservices.events.publisher"
})
public class ReviewServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);
    }
}
