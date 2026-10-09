package com.taller2.negocio.model.state;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;

/**
 * Patrón de Comportamiento GoF: State
 * Define la interfaz para encapsular el comportamiento y las reglas de transición asociadas
 * a un estado específico del ciclo de vida de una pregunta, así como su color de visualización.
 */
public interface QuestionState {
    
    /**
     * Nombre descriptivo del estado.
     */
    String getNombre();

    /**
     * Código hexadecimal de color para la visualización en la UI (Requisito HU2).
     */
    String getColorHex();

    /**
     * Mapeo al enum QuestionStatus para persistencia y compatibilidad.
     */
    QuestionStatus getStatusEnum();

    /**
     * Indica si la pregunta puede ser modificada por su autor en este estado.
     */
    boolean permiteEdicion();

    /**
     * Transición: Cambiar de estado a Pendiente de Revisión (HU2).
     */
    void enviarARevision(Question question);

    /**
     * Transición: El administrador asigna revisores (HU4).
     */
    void asignarRevisores(Question question, String revisor);

    /**
     * Transición: El revisor aprueba la pregunta (HU5).
     */
    void aprobar(Question question, String observacion);

    /**
     * Transición: El revisor rechaza la pregunta (HU5).
     */
    void rechazar(Question question, String observacion);
}
