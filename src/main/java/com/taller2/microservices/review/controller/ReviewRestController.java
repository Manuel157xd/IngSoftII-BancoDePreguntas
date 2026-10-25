package com.taller2.microservices.review.controller;

import com.taller2.microservices.events.model.QuestionEvaluatedEvent;
import com.taller2.microservices.events.model.ReviewersAssignedEvent;
import com.taller2.microservices.events.publisher.DomainEventPublisher;
import com.taller2.microservices.questionclient.QuestionServiceClient;
import com.taller2.microservices.questionclient.QuestionSnapshot;
import com.taller2.microservices.review.repository.ReviewMicroserviceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Contexto: Revisión por Pares (Peer Review)", description = "Microservicio para asignación de revisores y evaluación de preguntas (HU4 y HU5)")
@CrossOrigin(origins = "*")
public class ReviewRestController {

    private final ReviewMicroserviceRepository reviewRepository;
    private final QuestionServiceClient questionService;
    private final DomainEventPublisher eventPublisher;

    public ReviewRestController(
            ReviewMicroserviceRepository reviewRepository,
            QuestionServiceClient questionService,
            DomainEventPublisher eventPublisher
    ) {
        this.reviewRepository = reviewRepository;
        this.questionService = questionService;
        this.eventPublisher = eventPublisher;
    }

    public record AssignReviewersDTO(int preguntaId, List<String> revisoresEmails) {}
    public record EvaluateQuestionDTO(int preguntaId, String revisorEmail, String decision, String observaciones) {}

    @PostMapping("/assign")
    @Operation(summary = "Asignar revisores (HU4)", description = "Asigna entre 1 y 3 revisores y envía un evento HTTP al servicio de notificaciones.")
    public ResponseEntity<?> assignReviewers(@RequestBody AssignReviewersDTO dto) {
        if (dto.revisoresEmails() == null || dto.revisoresEmails().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "La regla de negocio exige asignar entre 1 y 3 revisores"
            ));
        }

        List<String> reviewerEmails = dto.revisoresEmails().stream()
                .filter(email -> email != null && !email.trim().isBlank())
                .map(email -> email.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .toList();

        if (reviewerEmails.isEmpty() || reviewerEmails.size() > 3
                || reviewerEmails.stream().anyMatch(email -> !email.contains("@"))) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "La regla de negocio exige asignar entre 1 y 3 correos de revisor válidos y únicos"
            ));
        }

        var questionOpt = questionService.findById(dto.preguntaId());
        if (questionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        QuestionSnapshot question = questionOpt.get();
        if (!"PENDIENTE_REVISION".equals(question.estado())
                && !"EN_REVISION".equals(question.estado())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Solo se pueden asignar revisores a preguntas pendientes de revisión"
            ));
        }

        QuestionSnapshot updatedQuestion;
        try {
            updatedQuestion = questionService.assignReviewers(question.id(), reviewerEmails);
        } catch (IllegalStateException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        }

        for (String email : reviewerEmails) {
            reviewRepository.createAssignment(updatedQuestion.id(), email);
        }
        eventPublisher.publish(new ReviewersAssignedEvent(
                updatedQuestion.id(), updatedQuestion.nombre(), reviewerEmails
        ));

        return ResponseEntity.ok(Map.of(
                "mensaje", "Revisores asignados y evento enviado al servicio de notificaciones",
                "preguntaId", updatedQuestion.id(),
                "nuevoEstado", updatedQuestion.estado(),
                "colorHex", updatedQuestion.colorHex(),
                "revisores", reviewerEmails
        ));
    }

    @GetMapping("/assigned/{revisorEmail}")
    @Operation(summary = "Listar preguntas asignadas al revisor (HU5)", description = "Consulta asignaciones y obtiene los detalles del servicio de preguntas.")
    public ResponseEntity<?> getAssignedQuestions(@PathVariable("revisorEmail") String reviewerEmail) {
        List<ReviewMicroserviceRepository.ReviewAssignment> assignments =
                reviewRepository.findByRevisor(reviewerEmail);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ReviewMicroserviceRepository.ReviewAssignment assignment : assignments) {
            questionService.findById(assignment.preguntaId()).ifPresent(question -> {
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("asignacionId", assignment.id());
                item.put("preguntaId", question.id());
                item.put("nombre", question.nombre());
                item.put("texto", question.texto());
                item.put("contexto", question.contexto());
                item.put("opciones", question.opciones());
                item.put("respuestaCorrecta", question.respuestaCorrecta());
                item.put("justificacion", question.justificacion());
                item.put("bibliografia", question.bibliografia());
                item.put("competencia", question.competencia());
                item.put("tema", question.tema());
                item.put("subtema", question.subtema());
                item.put("nivelDificultad", question.nivelDificultad());
                item.put("estadoPregunta", question.estado());
                item.put("colorHex", question.colorHex());
                item.put("estadoAsignacion", assignment.estado());
                item.put("observaciones", assignment.observaciones() == null ? "" : assignment.observaciones());
                result.add(item);
            });
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluar pregunta con observaciones (HU5)", description = "Registra el resultado y envía un evento HTTP al servicio de notificaciones.")
    public ResponseEntity<?> evaluateQuestion(@RequestBody EvaluateQuestionDTO dto) {
        if (dto.observaciones() == null || dto.observaciones().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Las observaciones del revisor son obligatorias"));
        }
        if (dto.revisorEmail() == null || dto.revisorEmail().isBlank() || dto.decision() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El revisor y la decisión son obligatorios"));
        }

        var questionOpt = questionService.findById(dto.preguntaId());
        if (questionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        boolean reviewerAssigned = reviewRepository.findByRevisor(dto.revisorEmail()).stream()
                .anyMatch(assignment -> assignment.preguntaId() == dto.preguntaId());
        if (!reviewerAssigned) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "El revisor no tiene asignada esta pregunta"
            ));
        }

        String decision = dto.decision().trim().toUpperCase(Locale.ROOT);
        if (!"APROBADA".equals(decision) && !"RECHAZADA".equals(decision)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "La decisión debe ser 'APROBADA' o 'RECHAZADA'"
            ));
        }

        QuestionSnapshot evaluatedQuestion;
        try {
            evaluatedQuestion = questionService.evaluate(
                    dto.preguntaId(), dto.revisorEmail(), decision, dto.observaciones()
            );
        } catch (IllegalStateException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        }

        reviewRepository.recordEvaluation(
                evaluatedQuestion.id(), dto.revisorEmail(), decision, dto.observaciones()
        );
        eventPublisher.publish(new QuestionEvaluatedEvent(
                evaluatedQuestion.id(), evaluatedQuestion.nombre(), dto.revisorEmail(),
                decision, dto.observaciones(), evaluatedQuestion.autorId()
        ));

        return ResponseEntity.ok(Map.of(
                "mensaje", "Evaluación registrada y evento de notificación publicado",
                "preguntaId", evaluatedQuestion.id(),
                "nuevoEstado", evaluatedQuestion.estado(),
                "colorHex", evaluatedQuestion.colorHex(),
                "observaciones", dto.observaciones()
        ));
    }
}
