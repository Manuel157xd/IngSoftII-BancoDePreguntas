package com.taller2.negocio.validation;

import com.taller2.negocio.model.Question;

public class QuestionValidator implements QuestionPolicy {
    private static final int REQUIRED_OPTION_COUNT = 4;

    @Override
    public void validar(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("La pregunta es obligatoria.");
        }
        if (isBlank(question.getNombre())) {
            throw new IllegalArgumentException("El nombre de la pregunta es obligatorio.");
        }
        if (isBlank(question.getContexto()) || isBlank(question.getTexto())
                || isBlank(question.getJustificacion())) {
            throw new IllegalArgumentException("Contexto, pregunta y justificación son obligatorios.");
        }
        if (isBlank(question.getCompetencia()) || isBlank(question.getTema())) {
            throw new IllegalArgumentException("Debe definir competencia y tema.");
        }
        if (isBlank(question.getBibliografia()) || isBlank(question.getSubtema())) {
            throw new IllegalArgumentException("Bibliografía y subtema son obligatorios.");
        }
        if (question.getOpciones() == null || question.getOpciones().size() != REQUIRED_OPTION_COUNT
                || question.getOpciones().stream().anyMatch(QuestionValidator::isBlank)) {
            throw new IllegalArgumentException("Debe ingresar exactamente 4 opciones no vacías.");
        }
        if (question.getRespuestaCorrecta() < 0
                || question.getRespuestaCorrecta() >= REQUIRED_OPTION_COUNT) {
            throw new IllegalArgumentException("La opción correcta debe estar entre 0 y 3.");
        }
        if (question.getEstado() == null || isBlank(question.getNivelDificultad())) {
            throw new IllegalArgumentException("El estado y el nivel de dificultad son obligatorios.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}