package com.taller2.negocio.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;

class QuestionValidatorTest {
    private final QuestionValidator validator = new QuestionValidator();

    @Test
    void aceptaUnaPreguntaCompleta() {
        assertDoesNotThrow(() -> validator.validar(pregunta()));
    }

    @Test
    void rechazaPreguntaSinNombre() {
        Question question = pregunta();
        question.setNombre(" ");

        assertThrows(IllegalArgumentException.class, () -> validator.validar(question));
    }

    @Test
    void rechazaPreguntaSinCuatroOpciones() {
        Question question = pregunta();
        question.setOpciones(List.of("A", "B", "C"));

        assertThrows(IllegalArgumentException.class, () -> validator.validar(question));
    }

    @Test
    void rechazaIndiceDeRespuestaFueraDeRango() {
        Question question = pregunta();
        question.setRespuestaCorrecta(4);

        assertThrows(IllegalArgumentException.class, () -> validator.validar(question));
    }

    @Test
    void rechazaPreguntaSinBibliografia() {
        Question question = pregunta();
        question.setBibliografia(" ");

        assertThrows(IllegalArgumentException.class, () -> validator.validar(question));
    }

    @Test
    void rechazaPreguntaSinSubtema() {
        Question question = pregunta();
        question.setSubtema(" ");

        assertThrows(IllegalArgumentException.class, () -> validator.validar(question));
    }

    private Question pregunta() {
        return new Question(
                "Pregunta", "Texto", List.of("A", "B", "C", "D"), 1,
                QuestionStatus.BORRADOR, "Contexto", "Justificación", "Bibliografía",
                "Competencia", "Tema", "Subtema", "Medio", null
        );
    }
}