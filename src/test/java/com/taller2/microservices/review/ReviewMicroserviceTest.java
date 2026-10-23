package com.taller2.microservices.review;

import com.taller2.microservices.events.DomainEventBus;
import com.taller2.microservices.questions.repository.QuestionMicroserviceRepository;
import com.taller2.microservices.questionclient.LocalQuestionServiceClient;
import com.taller2.microservices.review.controller.ReviewRestController;
import com.taller2.microservices.review.repository.ReviewMicroserviceRepository;
import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReviewMicroserviceTest {

    private ReviewMicroserviceRepository reviewRepo;
    private QuestionMicroserviceRepository questionRepo;
    private DomainEventBus eventBus;
    private ReviewRestController controller;

    @BeforeEach
    void setUp() {
        new File("test_reviews.db").delete();
        new File("test_questions.db").delete();

        reviewRepo = new ReviewMicroserviceRepository("jdbc:sqlite:test_reviews.db");
        questionRepo = new QuestionMicroserviceRepository("jdbc:sqlite:test_questions.db");
        eventBus = new DomainEventBus();
        controller = new ReviewRestController(
                reviewRepo, new LocalQuestionServiceClient(questionRepo), eventBus
        );
    }

    @Test
    void testAsignacionValidaDeUnoATresRevisoresHU4() {
        // Crear una pregunta en estado PENDIENTE_REVISION
        Question q = Question.builder()
                .conNombre("Pregunta de Prueba")
                .conTexto("Texto")
                .conContexto("Contexto")
                .conOpciones(List.of("A", "B", "C", "D"))
                .conRespuestaCorrecta(0)
                .conEstado(QuestionStatus.PENDIENTE_REVISION)
                .build();
        Question guardada = questionRepo.save(q);

        // HU4: Asignar 2 revisores (entre 1 y 3 permitido)
        ResponseEntity<?> resp = controller.assignReviewers(
                new ReviewRestController.AssignReviewersDTO(guardada.getId(), List.of("revisor1@unicauca.edu.co", "revisor2@unicauca.edu.co"))
        );

        assertEquals(200, resp.getStatusCode().value());

        // Verificar que el estado cambió a EN_REVISION y su color es #17A2B8
        Question actualizada = questionRepo.findById(guardada.getId()).orElseThrow();
        assertEquals(QuestionStatus.EN_REVISION, actualizada.getEstado());
        assertEquals("#17A2B8", actualizada.getColorHex());
    }

    @Test
    void testAsignacionInvalidaMasDeTresRevisoresRechazadaHU4() {
        Question q = Question.builder()
                .conNombre("Pregunta")
                .conTexto("Texto")
                .conContexto("Contexto")
                .conOpciones(List.of("A", "B", "C", "D"))
                .conRespuestaCorrecta(0)
                .conEstado(QuestionStatus.PENDIENTE_REVISION)
                .build();
        Question guardada = questionRepo.save(q);

        // Intentar asignar 4 revisores (debe fallar)
        ResponseEntity<?> resp = controller.assignReviewers(
                new ReviewRestController.AssignReviewersDTO(guardada.getId(), List.of("r1@u.co", "r2@u.co", "r3@u.co", "r4@u.co"))
        );

        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    void testEvaluacionAprobacionHU5() {
        Question q = Question.builder()
                .conNombre("Pregunta Calificable")
                .conTexto("Texto")
                .conContexto("Contexto")
                .conOpciones(List.of("A", "B", "C", "D"))
                .conRespuestaCorrecta(0)
                .conEstado(QuestionStatus.EN_REVISION)
                .build();
        Question guardada = questionRepo.save(q);
        reviewRepo.createAssignment(guardada.getId(), "revisor1@unicauca.edu.co");

        ResponseEntity<?> resp = controller.evaluateQuestion(
                new ReviewRestController.EvaluateQuestionDTO(guardada.getId(), "revisor1@unicauca.edu.co", "APROBADA", "Excelente redacción.")
        );

        assertEquals(200, resp.getStatusCode().value());

        Question evaluada = questionRepo.findById(guardada.getId()).orElseThrow();
        assertEquals(QuestionStatus.APROBADA, evaluada.getEstado());
        assertEquals("#28A745", evaluada.getColorHex());
    }

    @Test
    void testRevisorSinAsignacionNoPuedeEvaluar() {
        Question q = Question.builder()
                .conNombre("Pregunta protegida")
                .conTexto("Texto")
                .conContexto("Contexto")
                .conOpciones(List.of("A", "B", "C", "D"))
                .conRespuestaCorrecta(0)
                .conEstado(QuestionStatus.EN_REVISION)
                .build();
        Question guardada = questionRepo.save(q);

        ResponseEntity<?> response = controller.evaluateQuestion(
                new ReviewRestController.EvaluateQuestionDTO(
                        guardada.getId(), "revisor-no-asignado@unicauca.edu.co",
                        "APROBADA", "Evaluación no autorizada."
                )
        );

        assertEquals(403, response.getStatusCode().value());
        assertEquals(
                QuestionStatus.EN_REVISION,
                questionRepo.findById(guardada.getId()).orElseThrow().getEstado()
        );
    }
}
