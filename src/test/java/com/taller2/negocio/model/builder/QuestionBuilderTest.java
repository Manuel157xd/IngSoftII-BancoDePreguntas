package com.taller2.negocio.model.builder;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestionBuilderTest {

    @Test
    void testConstruccionCompletaConBuilder() {
        Question q = QuestionBuilder.builder()
                .conId(10)
                .conNombre("Pregunta sobre Algoritmos")
                .conContexto("Dado un grafo no dirigido con pesos positivos...")
                .conTexto("¿Cuál es la complejidad temporal de Dijkstra con min-heap?")
                .conOpciones(List.of("O(V^2)", "O(E log V)", "O(V log E)", "O(E + V)"))
                .conRespuestaCorrecta(1)
                .conJustificacion("Usando una cola de prioridad binaria, cada arista toma O(log V).")
                .conBibliografia("Cormen et al. Introduction to Algorithms.")
                .conCompetencia("Diseño y análisis de algoritmos")
                .conTema("Grafos")
                .conSubtema("Caminos más cortos")
                .conNivelDificultad("Avanzado")
                .conEstado(QuestionStatus.BORRADOR)
                .conAutorId(1)
                .build();

        assertNotNull(q);
        assertEquals(10, q.getId());
        assertEquals("Pregunta sobre Algoritmos", q.getNombre());
        assertEquals("Dado un grafo no dirigido con pesos positivos...", q.getContexto());
        assertEquals("¿Cuál es la complejidad temporal de Dijkstra con min-heap?", q.getTexto());
        assertEquals(4, q.getOpciones().size());
        assertEquals("O(E log V)", q.getOpciones().get(1));
        assertEquals(1, q.getRespuestaCorrecta());
        assertEquals("Diseño y análisis de algoritmos", q.getCompetencia());
        assertEquals("Grafos", q.getTema());
        assertEquals("Caminos más cortos", q.getSubtema());
        assertEquals("Avanzado", q.getNivelDificultad());
        assertEquals(QuestionStatus.BORRADOR, q.getEstado());
        assertEquals(1, q.getAutorId());
    }

    @Test
    void testAgregarOpcionesIndividualmente() {
        Question q = Question.builder()
                .conNombre("Pregunta Matemática")
                .conTexto("¿Cuánto es 2 + 2?")
                .agregarOpcion("3")
                .agregarOpcion("4")
                .agregarOpcion("5")
                .agregarOpcion("6")
                .conRespuestaCorrecta(1)
                .build();

        assertEquals(4, q.getOpciones().size());
        assertEquals("4", q.getOpciones().get(1));
        assertEquals(QuestionStatus.BORRADOR, q.getEstado());
    }
}
