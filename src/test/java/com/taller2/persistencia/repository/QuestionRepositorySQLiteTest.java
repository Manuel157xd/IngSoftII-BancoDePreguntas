package com.taller2.persistencia.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;

class QuestionRepositorySQLiteTest {
    private static final String URL = "jdbc:sqlite:taller2-test.db";
    private QuestionRepositorySQLite repository;

    @BeforeEach
    void prepararBaseDeDatos() throws Exception {
        repository = new QuestionRepositorySQLite(URL);
        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {
            
            // Eliminar tabla anterior para asegurar que se pruebe con el nuevo esquema
            statement.execute("DROP TABLE IF EXISTS preguntas");

            // Crear tabla de pruebas con todas las columnas DCE
            statement.execute("""
                    CREATE TABLE preguntas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nombre TEXT NOT NULL,
                        texto TEXT NOT NULL,
                        opciones TEXT NOT NULL,
                        respuesta_correcta INTEGER NOT NULL,
                        estado TEXT NOT NULL,
                        contexto TEXT,
                        justificacion TEXT,
                        bibliografia TEXT,
                        competencia TEXT,
                        tema TEXT,
                        subtema TEXT,
                        nivel_dificultad TEXT,
                        revisor_asignado TEXT
                    )
                    """);
        }
    }

    @Test
    void debeGuardarYLeerPreguntaConOpciones() {
        Question nuevaPregunta = new Question(
                "Saber Pro 1",
                "¿Cuál es la opción correcta?",
                List.of("A", "B", "C", "D"),
                1,
                QuestionStatus.BORRADOR,
                "Texto de contexto de prueba",
                "Justificación de la respuesta correcta",
                "Bibliografía de referencia",
                "Razonamiento Cuantitativo",
                "Matemáticas",
                "Álgebra",
                "Medio",
                null
        );

        repository.guardar(nuevaPregunta);

        Question question = repository.listarTodas().get(0);
        assertEquals("Saber Pro 1", question.getNombre());
        assertEquals(List.of("A", "B", "C", "D"), question.getOpciones());
        assertEquals(QuestionStatus.BORRADOR, question.getEstado());
        assertEquals("Texto de contexto de prueba", question.getContexto());
        assertEquals("Matemáticas", question.getTema());
        assertEquals("Razonamiento Cuantitativo", question.getCompetencia());
    }

    @Test
    void debeActualizarEstadoYFiltrar() {
        Question nuevaPregunta = new Question(
                "Pregunta 1",
                "Texto de la pregunta",
                List.of("Opción A", "Opción B", "Opción C", "Opción D"),
                0,
                QuestionStatus.BORRADOR,
                "Contexto de la pregunta",
                "Justificación de respuesta",
                "Bibliografía de referencia",
                "Competencia evaluada",
                "Tema principal",
                "Subtema específico",
                "Medio",
                null
        );

        repository.guardar(nuevaPregunta);

        Question question = repository.listarTodas().get(0);
        question.setEstado(QuestionStatus.PENDIENTE_REVISION);
        repository.actualizar(question);

        assertTrue(repository.listarPorEstado(QuestionStatus.BORRADOR).isEmpty());
        assertEquals(1, repository.listarPorEstado(QuestionStatus.PENDIENTE_REVISION).size());
    }
}