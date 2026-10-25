package com.taller2.presentacion.controller;

import com.taller2.negocio.model.QuestionStatus;
import com.taller2.negocio.service.ReviewGateway;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReviewControllerTest {

    @Test
    void debeListarPreguntasAsignadasAlRevisor() {
        ReviewGateway.AssignedQuestion assigned = new ReviewGateway.AssignedQuestion(
                1, 10, "Pregunta DCE", "Texto", "Contexto", List.of("A", "B", "C", "D"),
                0, "Justificación", "Bibliografía", "Competencia", "Tema", "Subtema",
                "Medio", "EN_REVISION", "#17A2B8", "PENDIENTE", ""
        );

        ReviewGateway mockGateway = new ReviewGateway() {
            @Override
            public QuestionStatus assignReviewers(int questionId, List<String> reviewerEmails) {
                return QuestionStatus.EN_REVISION;
            }

            @Override
            public List<AssignedQuestion> listAssignedQuestions(String reviewerEmail) {
                return List.of(assigned);
            }

            @Override
            public void evaluate(int questionId, String reviewerEmail, String decision, String observation) {}
        };

        ReviewController controller = new ReviewController(mockGateway);
        List<ReviewGateway.AssignedQuestion> result = controller.listAssignedQuestions("revisor@unicauca.edu.co");

        assertEquals(1, result.size());
        assertEquals("Pregunta DCE", result.get(0).name());
        assertEquals(10, result.get(0).questionId());
    }

    @Test
    void debeDelegarEvaluacionAlGateway() {
        List<String> records = new ArrayList<>();
        ReviewGateway mockGateway = new ReviewGateway() {
            @Override
            public QuestionStatus assignReviewers(int questionId, List<String> reviewerEmails) {
                return QuestionStatus.EN_REVISION;
            }

            @Override
            public List<AssignedQuestion> listAssignedQuestions(String reviewerEmail) {
                return List.of();
            }

            @Override
            public void evaluate(int questionId, String reviewerEmail, String decision, String observation) {
                records.add(questionId + "|" + reviewerEmail + "|" + decision + "|" + observation);
            }
        };

        ReviewController controller = new ReviewController(mockGateway);
        controller.evaluate(15, "revisor@unicauca.edu.co", "APROBADA", "Muy bien formulada.");

        assertEquals(1, records.size());
        assertEquals("15|revisor@unicauca.edu.co|APROBADA|Muy bien formulada.", records.get(0));
    }
}
