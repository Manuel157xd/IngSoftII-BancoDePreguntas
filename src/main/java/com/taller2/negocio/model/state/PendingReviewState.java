package com.taller2.negocio.model.state;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;

public class PendingReviewState implements QuestionState {

    @Override
    public String getNombre() {
        return "Pendiente de Revisión";
    }

    @Override
    public String getColorHex() {
        return "#FFC107"; // Amarillo / Ámbar
    }

    @Override
    public QuestionStatus getStatusEnum() {
        return QuestionStatus.PENDIENTE_REVISION;
    }

    @Override
    public boolean permiteEdicion() {
        return false;
    }

    @Override
    public void enviarARevision(Question question) {
        // Ya se encuentra en este estado
    }

    @Override
    public void asignarRevisores(Question question, String revisor) {
        question.setRevisorAsignado(revisor);
        question.setEstado(QuestionStatus.EN_REVISION);
    }

    @Override
    public void aprobar(Question question, String observacion) {
        throw new IllegalStateException("La pregunta debe estar En Revisión antes de ser evaluada.");
    }

    @Override
    public void rechazar(Question question, String observacion) {
        throw new IllegalStateException("La pregunta debe estar En Revisión antes de ser evaluada.");
    }
}
