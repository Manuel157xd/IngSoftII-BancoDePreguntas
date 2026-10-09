package com.taller2.negocio.model.state;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;

public class UnderReviewState implements QuestionState {

    @Override
    public String getNombre() {
        return "En Revisión";
    }

    @Override
    public String getColorHex() {
        return "#17A2B8"; // Azul cian / informativo
    }

    @Override
    public QuestionStatus getStatusEnum() {
        return QuestionStatus.EN_REVISION;
    }

    @Override
    public boolean permiteEdicion() {
        return false;
    }

    @Override
    public void enviarARevision(Question question) {
        throw new IllegalStateException("La pregunta ya se encuentra en proceso de revisión.");
    }

    @Override
    public void asignarRevisores(Question question, String revisor) {
        question.setRevisorAsignado(revisor);
    }

    @Override
    public void aprobar(Question question, String observacion) {
        if (observacion != null && !observacion.isBlank()) {
            question.setJustificacion(question.getJustificacion() + " [Obs. Aprobación: " + observacion + "]");
        }
        question.setEstado(QuestionStatus.APROBADA);
    }

    @Override
    public void rechazar(Question question, String observacion) {
        if (observacion != null && !observacion.isBlank()) {
            question.setJustificacion(question.getJustificacion() + " [Obs. Rechazo: " + observacion + "]");
        }
        question.setEstado(QuestionStatus.RECHAZADA);
    }
}
