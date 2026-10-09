package com.taller2.negocio.model.state;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;

public class ApprovedState implements QuestionState {

    @Override
    public String getNombre() {
        return "Aprobada";
    }

    @Override
    public String getColorHex() {
        return "#28A745"; // Verde éxito
    }

    @Override
    public QuestionStatus getStatusEnum() {
        return QuestionStatus.APROBADA;
    }

    @Override
    public boolean permiteEdicion() {
        return false;
    }

    @Override
    public void enviarARevision(Question question) {
        throw new IllegalStateException("Una pregunta aprobada no puede volver a revisión directamente.");
    }

    @Override
    public void asignarRevisores(Question question, String revisor) {
        throw new IllegalStateException("Una pregunta aprobada ya ha sido evaluada.");
    }

    @Override
    public void aprobar(Question question, String observacion) {
        // Ya está aprobada
    }

    @Override
    public void rechazar(Question question, String observacion) {
        throw new IllegalStateException("No se puede rechazar una pregunta que ya ha sido aprobada.");
    }
}
