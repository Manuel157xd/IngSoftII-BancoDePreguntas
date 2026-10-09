package com.taller2.negocio.model.state;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;

public class ArchivedState implements QuestionState {

    @Override
    public String getNombre() {
        return "Archivada";
    }

    @Override
    public String getColorHex() {
        return "#343A40"; // Gris oscuro
    }

    @Override
    public QuestionStatus getStatusEnum() {
        return QuestionStatus.ARCHIVADA;
    }

    @Override
    public boolean permiteEdicion() {
        return false;
    }

    @Override
    public void enviarARevision(Question question) {
        throw new IllegalStateException("Una pregunta archivada no puede reactivarse directamente a revisión.");
    }

    @Override
    public void asignarRevisores(Question question, String revisor) {
        throw new IllegalStateException("Una pregunta archivada no admite revisores.");
    }

    @Override
    public void aprobar(Question question, String observacion) {
        throw new IllegalStateException("Una pregunta archivada no puede ser aprobada.");
    }

    @Override
    public void rechazar(Question question, String observacion) {
        throw new IllegalStateException("Una pregunta archivada no puede ser rechazada.");
    }
}
