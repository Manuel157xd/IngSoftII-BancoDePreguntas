package com.taller2.microservices.validation;

import com.taller2.microservices.validation.facade.StructuralValidationFacade;
import com.taller2.microservices.validation.model.ValidationRequest;
import com.taller2.microservices.validation.model.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StructuralValidationFacadeTest {

    private StructuralValidationFacade facade;

    @BeforeEach
    void setUp() {
        facade = new StructuralValidationFacade();
    }

    @Test
    void testPreguntaValidaDCE() {
        ValidationRequest req = new ValidationRequest(
                "Pregunta Grafos",
                "¿Cuál es el algoritmo más eficiente para árboles de expansión mínima?",
                "Dado un grafo conexo ponderado no dirigido con pesos arbitrarios...",
                List.of("Kruskal", "Prim con heap de Fibonacci", "Floyd-Warshall", "Bellman-Ford"),
                1,
                "Prim con heap de Fibonacci alcanza complejidad O(E + V log V).",
                "Cormen et al. Introduction to Algorithms.",
                "Estructuras de Datos y Algoritmos",
                "Grafos",
                "Árboles de Expansión",
                "Avanzado"
        );

        ValidationResult result = facade.validateQuestion(req);
        assertTrue(result.valid(), "Debe ser válida cuando cumple todas las reglas DCE");
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void testPreguntaInvalidaMenosDeCuatroDistractores() {
        ValidationRequest req = new ValidationRequest(
                "Pregunta Incompleta",
                "¿Cuánto es 2 + 2?",
                "Contexto de prueba para verificación...",
                List.of("3", "4", "5"), // Solo 3 opciones en vez de 4
                1,
                "2+2 es 4",
                "Aritmética básica",
                "Matemáticas",
                "Aritmética",
                "Suma",
                "Fácil"
        );

        ValidationResult result = facade.validateQuestion(req);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.field().equals("opciones")));
    }

    @Test
    void testPreguntaInvalidaSinContextoDCE() {
        ValidationRequest req = new ValidationRequest(
                "Pregunta Sin Contexto",
                "¿Cuál es la capital de Colombia?",
                "", // Contexto vacío
                List.of("Medellín", "Bogotá", "Cali", "Cartagena"),
                1,
                "Bogotá es el distrito capital.",
                "Geografía política",
                "Ciencias Sociales",
                "Geografía",
                "Capitales",
                "Fácil"
        );

        ValidationResult result = facade.validateQuestion(req);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.field().equals("contexto")));
    }
}
