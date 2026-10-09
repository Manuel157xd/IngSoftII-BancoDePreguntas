package com.taller2.negocio.model.state;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;

public class DraftState implements QuestionState {

    @Override
    public String getNombre() {
        return "Borrador";
    }

    @Override
    public String getColorHex() {
        return "#6C757D"; // Gris neutro
    }

    @Override
    public QuestionStatus getStatusEnum() {
        return QuestionStatus.BORRADOR;
    }

    @Override
    public boolean permiteEdicion() {
        return true;
    }

    @Override
    public void enviarARevision(Question question) {
        question.setEstado(QuestionStatus.PENDIENTE_REVISION);
    }

    @Override
    public void asignarRevisores(Question question, String revisor) {
        throw new IllegalStateException("No se pueden asignar revisores a una pregunta en estado Borrador.");
    }

    @Override
    public void aprobar(Question question, String observacion) {
        throw new IllegalStateException("No se puede aprobar una pregunta directamente desde Borrador.");
    }

    @Override
    public void rechazar(Question question, String observacion) {
        throw new IllegalStateException("No se puede rechazar una pregunta directamente desde Borrador.");
    }
}
