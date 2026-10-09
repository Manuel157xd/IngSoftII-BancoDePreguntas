package com.taller2.negocio.model.state;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;

public class PublishedState implements QuestionState {

    @Override
    public String getNombre() {
        return "Publicada";
    }

    @Override
    public String getColorHex() {
        return "#6F42C1"; // Morado
    }

    @Override
    public QuestionStatus getStatusEnum() {
        return QuestionStatus.PUBLICADA;
    }

    @Override
    public boolean permiteEdicion() {
        return false;
    }

    @Override
    public void enviarARevision(Question question) {
        throw new IllegalStateException("Una pregunta ya publicada no puede enviarse a revisión.");
    }

    @Override
    public void asignarRevisores(Question question, String revisor) {
        throw new IllegalStateException("Una pregunta publicada no admite asignación de revisores.");
    }

    @Override
    public void aprobar(Question question, String observacion) {
        // Ya está publicada
    }

    @Override
    public void rechazar(Question question, String observacion) {
        throw new IllegalStateException("No se puede rechazar una pregunta publicada.");
    }
}
