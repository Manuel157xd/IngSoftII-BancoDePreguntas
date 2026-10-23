package com.taller2.microservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.taller2.microservices",
        "com.taller2.negocio"
})
public class MicroservicesApplication {

    public static void main(String[] args) {
        System.setProperty("spring.classformat.ignore", "true");
        SpringApplication.run(MicroservicesApplication.class, args);
        System.out.println("==================================================================");
        System.out.println("🚀 Microservicios y API Gateway iniciados con éxito.");
        System.out.println("📖 Swagger UI disponible en: http://localhost:8080/swagger-ui/index.html");
        System.out.println("📄 Documentación OpenAPI JSON en: http://localhost:8080/v3/api-docs");
        System.out.println("==================================================================");
    }
}
