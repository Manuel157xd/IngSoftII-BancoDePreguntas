package com.taller2.microservices.review.controller;

import com.taller2.microservices.events.DomainEventBus;
import com.taller2.microservices.events.model.QuestionEvaluatedEvent;
import com.taller2.microservices.events.model.ReviewersAssignedEvent;
import com.taller2.microservices.questions.repository.QuestionMicroserviceRepository;
import com.taller2.microservices.review.repository.ReviewMicroserviceRepository;
import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;
import com.taller2.negocio.model.state.QuestionState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Contexto: Revisión por Pares (Peer Review)", description = "Microservicio para asignación de 1 a 3 revisores y evaluación de preguntas (HU4 y HU5)")
@CrossOrigin(origins = "*")
public class ReviewRestController {

    private final ReviewMicroserviceRepository reviewRepository;
    private final QuestionMicroserviceRepository questionRepository;
    private final DomainEventBus eventBus;

    public ReviewRestController(ReviewMicroserviceRepository reviewRepository,
                                QuestionMicroserviceRepository questionRepository,
                                DomainEventBus eventBus) {
        this.reviewRepository = reviewRepository;
        this.questionRepository = questionRepository;
        this.eventBus = eventBus;
    }

    public record AssignReviewersDTO(int preguntaId, List<String> revisoresEmails) {}
    public record EvaluateQuestionDTO(int preguntaId, String revisorEmail, String decision, String observaciones) {}

    @PostMapping("/assign")
    @Operation(summary = "Asignar revisores (HU4)", description = "Permite al Administrador asignar entre 1 y 3 revisores a una pregunta 'Pendiente de revisión' y notifica por email.")
    public ResponseEntity<?> assignReviewers(@RequestBody AssignReviewersDTO dto) {
        // Validación obligatoria HU4: entre 1 y 3 revisores
        if (dto.revisoresEmails() == null || dto.revisoresEmails().isEmpty() || dto.revisoresEmails().size() > 3) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "La regla de negocio exige asignar entre 1 y 3 revisores (recibidos: " +
                            (dto.revisoresEmails() == null ? 0 : dto.revisoresEmails().size()) + ")"
            ));
        }

        Optional<Question> opt = questionRepository.findById(dto.preguntaId());
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Question q = opt.get();
        if (q.getEstado() != QuestionStatus.PENDIENTE_REVISION && q.getEstado() != QuestionStatus.EN_REVISION) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Solo se pueden asignar revisores a preguntas en estado PENDIENTE_REVISION"
            ));
        }

        // Transición de estado con Patrón State
        QuestionState state = q.getStateObject();
        String revisoresConcatenados = String.join(", ", dto.revisoresEmails());
        try {
            state.asignarRevisores(q, revisoresConcatenados);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        questionRepository.update(q);

        // Guardar cada asignación en reviews.db
        for (String email : dto.revisoresEmails()) {
            reviewRepository.createAssignment(q.getId(), email.trim());
        }

        // Emitir evento asíncrono para notificaciones por email (EDA / Observer)
        eventBus.publish(new ReviewersAssignedEvent(q.getId(), q.getNombre(), dto.revisoresEmails()));

        return ResponseEntity.ok(Map.of(
                "mensaje", "Revisores asignados correctamente y notificaciones enviadas por correo",
                "preguntaId", q.getId(),
                "nuevoEstado", q.getEstado().name(),
                "colorHex", q.getColorHex(),
                "revisores", dto.revisoresEmails()
        ));
    }

    @GetMapping("/assigned/{revisorEmail}")
    @Operation(summary = "Listar preguntas asignadas al revisor (HU5)", description = "Permite al docente revisor consultar las preguntas asignadas para su evaluación.")
    public ResponseEntity<?> getAssignedQuestions(@PathVariable String revisorEmail) {
        List<ReviewMicroserviceRepository.ReviewAssignment> assignments = reviewRepository.findByRevisor(revisorEmail);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ReviewMicroserviceRepository.ReviewAssignment a : assignments) {
            Optional<Question> qOpt = questionRepository.findById(a.preguntaId());
            if (qOpt.isPresent()) {
                Question q = qOpt.get();
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("asignacionId", a.id());
                item.put("preguntaId", q.getId());
                item.put("nombre", q.getNombre());
                item.put("texto", q.getTexto());
                item.put("contexto", q.getContexto());
                item.put("opciones", q.getOpciones());
                item.put("justificacion", q.getJustificacion());
                item.put("bibliografia", q.getBibliografia());
                item.put("competencia", q.getCompetencia());
                item.put("tema", q.getTema());
                item.put("nivelDificultad", q.getNivelDificultad());
                item.put("estadoPregunta", q.getEstado().name());
                item.put("colorHex", q.getColorHex());
                item.put("estadoAsignacion", a.estado());
                item.put("observaciones", a.observaciones() != null ? a.observaciones() : "");
                result.add(item);
            }
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluar pregunta con observaciones (HU5)", description = "El revisor deja observaciones y cambia el estado a Aprobada o Rechazada; se notifica al autor.")
    public ResponseEntity<?> evaluateQuestion(@RequestBody EvaluateQuestionDTO dto) {
        if (dto.observaciones() == null || dto.observaciones().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Las observaciones del revisor son obligatorias"));
        }

        Optional<Question> opt = questionRepository.findById(dto.preguntaId());
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Question q = opt.get();
        QuestionState state = q.getStateObject();

        String decision = dto.decision().trim().toUpperCase();
        try {
            if ("APROBADA".equals(decision)) {
                state.aprobar(q, dto.observaciones());
            } else if ("RECHAZADA".equals(decision)) {
                state.rechazar(q, dto.observaciones());
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "La decisión debe ser 'APROBADA' o 'RECHAZADA'"));
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        questionRepository.update(q);
        reviewRepository.recordEvaluation(q.getId(), dto.revisorEmail(), decision, dto.observaciones());

        // Emitir evento de dominio para notificar al autor (EDA)
        eventBus.publish(new QuestionEvaluatedEvent(
                q.getId(), q.getNombre(), dto.revisorEmail(), decision, dto.observaciones(), q.getAutorId()
        ));

        return ResponseEntity.ok(Map.of(
                "mensaje", "Evaluación registrada exitosamente y notificación enviada al autor",
                "preguntaId", q.getId(),
                "nuevoEstado", q.getEstado().name(),
                "colorHex", q.getColorHex(),
                "observaciones", dto.observaciones()
        ));
    }
}
