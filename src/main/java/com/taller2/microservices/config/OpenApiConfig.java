package com.taller2.microservices.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banco de Preguntas Saber Pro - API de Microservicios")
                        .version("2.0.0 (Corte 2)")
                        .description("""
                            API REST de la arquitectura distribuida por Contextos Delimitados (Bounded Contexts):
                            - **Identity & Access**: Autenticación y gestión de roles.
                            - **Question Management**: Creación DCE con Builder, filtros y paginación.
                            - **Structural Validation**: Validación de preguntas con Facade.
                            - **Peer Review**: Asignación de 1 a 3 revisores y evaluación con State.
                            - **Event-Driven Notifications**: Notificaciones asíncronas por email con Observer y Adapter.
                            """)
                        .contact(new Contact()
                                .name("Ingeniería de Software II - Universidad del Cauca")
                                .email("soporte-preguntas@unicauca.edu.co"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }
}
