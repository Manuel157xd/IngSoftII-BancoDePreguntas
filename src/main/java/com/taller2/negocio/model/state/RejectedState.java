package com.taller2.negocio.model.state;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;

public class RejectedState implements QuestionState {

    @Override
    public String getNombre() {
        return "Rechazada";
    }

    @Override
    public String getColorHex() {
        return "#DC3545"; // Rojo peligro/rechazo
    }

    @Override
    public QuestionStatus getStatusEnum() {
        return QuestionStatus.RECHAZADA;
    }

    @Override
    public boolean permiteEdicion() {
        return true; // Puede ser re-editada por el autor para corregir observaciones
    }

    @Override
    public void enviarARevision(Question question) {
        question.setEstado(QuestionStatus.PENDIENTE_REVISION);
    }

    @Override
    public void asignarRevisores(Question question, String revisor) {
        throw new IllegalStateException("La pregunta rechazada debe corregirse y enviarse a revisión antes de asignar revisores.");
    }

    @Override
    public void aprobar(Question question, String observacion) {
        throw new IllegalStateException("Una pregunta rechazada no puede ser aprobada directamente.");
    }

    @Override
    public void rechazar(Question question, String observacion) {
        // Ya está rechazada
    }
}
