package com.taller2.negocio.model.state;

import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestionStateTest {

    private Question crearPreguntaBorrador() {
        return Question.builder()
                .conId(1)
                .conNombre("Pregunta Demo")
                .conTexto("Texto de prueba")
                .conOpciones(List.of("A", "B", "C", "D"))
                .conRespuestaCorrecta(0)
                .conEstado(QuestionStatus.BORRADOR)
                .build();
    }

    @Test
    void testTransicionesDeEstadoExitosas() {
        Question q = crearPreguntaBorrador();

        // 1. Estado inicial Borrador (HU2)
        QuestionState state = q.getStateObject();
        assertTrue(state instanceof DraftState);
        assertEquals("#6C757D", q.getColorHex());
        assertTrue(state.permiteEdicion());

        // 2. Transición a Pendiente de Revisión (HU2)
        state.enviarARevision(q);
        assertEquals(QuestionStatus.PENDIENTE_REVISION, q.getEstado());
        assertEquals("#FFC107", q.getColorHex());
        assertFalse(q.getStateObject().permiteEdicion());

        // 3. Asignación de revisor por administrador (HU4)
        q.getStateObject().asignarRevisores(q, "profesor.revisor@unicauca.edu.co");
        assertEquals(QuestionStatus.EN_REVISION, q.getEstado());
        assertEquals("profesor.revisor@unicauca.edu.co", q.getRevisorAsignado());
        assertEquals("#17A2B8", q.getColorHex());

        // 4. Evaluación y Aprobación por revisor (HU5)
        q.getStateObject().aprobar(q, "Excelente redacción.");
        assertEquals(QuestionStatus.APROBADA, q.getEstado());
        assertEquals("#28A745", q.getColorHex());
        assertFalse(q.getStateObject().permiteEdicion());
    }

    @Test
    void testTransicionRechazo() {
        Question q = crearPreguntaBorrador();
        q.setEstado(QuestionStatus.EN_REVISION);

        q.getStateObject().rechazar(q, "Faltan referencias bibliográficas.");
        assertEquals(QuestionStatus.RECHAZADA, q.getEstado());
        assertEquals("#DC3545", q.getColorHex());
        assertTrue(q.getStateObject().permiteEdicion());
    }

    @Test
    void testTransicionesInvalidasLanzanExcepcion() {
        Question q = crearPreguntaBorrador();
        // Intentar aprobar directamente desde borrador debe fallar
        assertThrows(IllegalStateException.class, () -> q.getStateObject().aprobar(q, "No permitido"));
        // Intentar asignar revisores a un borrador debe fallar
        assertThrows(IllegalStateException.class, () -> q.getStateObject().asignarRevisores(q, "revisor@unicauca.edu.co"));
    }
}
