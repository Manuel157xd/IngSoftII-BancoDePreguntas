package com.taller2.microservices.questions.controller;

import com.taller2.microservices.events.model.QuestionSubmittedEvent;
import com.taller2.microservices.events.publisher.DomainEventPublisher;
import com.taller2.microservices.questions.repository.QuestionMicroserviceRepository;
import com.taller2.microservices.validation.facade.StructuralValidationFacade;
import com.taller2.microservices.validation.model.ValidationRequest;
import com.taller2.microservices.validation.model.ValidationResult;
import com.taller2.negocio.model.Question;
import com.taller2.negocio.model.QuestionStatus;
import com.taller2.negocio.model.builder.QuestionBuilder;
import com.taller2.negocio.model.state.QuestionState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "Contexto: Gestión de Preguntas (Question Management)", description = "Microservicio para creación DCE, filtros, paginación y ciclo de vida de preguntas")
@CrossOrigin(origins = "*")
public class QuestionRestController {

    private final QuestionMicroserviceRepository repository;
    private final StructuralValidationFacade validationFacade;
    private final DomainEventPublisher eventPublisher;

    public QuestionRestController(QuestionMicroserviceRepository repository,
                                  StructuralValidationFacade validationFacade,
                                  DomainEventPublisher eventPublisher) {
        System.out.println(">>> [INIT] QuestionRestController iniciado exitosamente.");
        this.repository = repository;
        this.validationFacade = validationFacade;
        this.eventPublisher = eventPublisher;
    }

    public record AssignReviewersRequest(List<String> reviewerEmails) {}
    public record EvaluateQuestionRequest(String reviewerEmail, String decision, String observation) {}
    public record UpdateQuestionRequest(
            String nombre,
            String texto,
            String contexto,
            List<String> opciones,
            int respuestaCorrecta,
            String justificacion,
            String bibliografia,
            String competencia,
            String tema,
            String subtema,
            String nivelDificultad,
            String estado,
            String revisorAsignado,
            Integer autorId
    ) {}

    public record CreateQuestionDTO(
            String nombre,
            String texto,
            String contexto,
            List<String> opciones,
            int respuestaCorrecta,
            String justificacion,
            String bibliografia,
            String competencia,
            String tema,
            String subtema,
            String nivelDificultad,
            Integer autorId
    ) {}

    @PostMapping
    @Operation(summary = "Crear pregunta DCE (HU1)", description = "Crea una pregunta usando el Patrón Builder tras validación estructural obligatoria.")
    public ResponseEntity<?> createQuestion(@RequestBody CreateQuestionDTO dto) {
        // 1. Validación estructural previa obligatoria (HU1 / Requisito 1)
        ValidationRequest vReq = new ValidationRequest(
                dto.nombre(), dto.texto(), dto.contexto(), dto.opciones(),
                dto.respuestaCorrecta(), dto.justificacion(), dto.bibliografia(),
                dto.competencia(), dto.tema(), dto.subtema(), dto.nivelDificultad()
        );
        ValidationResult valResult = validationFacade.validateQuestion(vReq);
        if (!valResult.valid()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Validación estructural fallida",
                    "detalles", valResult.errors()
            ));
        }

        // 2. Construcción con Patrón Creacional GoF Builder
        Question question = QuestionBuilder.builder()
                .conNombre(dto.nombre())
                .conTexto(dto.texto())
                .conContexto(dto.contexto())
                .conOpciones(dto.opciones())
                .conRespuestaCorrecta(dto.respuestaCorrecta())
                .conJustificacion(dto.justificacion())
                .conBibliografia(dto.bibliografia())
                .conCompetencia(dto.competencia())
                .conTema(dto.tema())
                .conSubtema(dto.subtema())
                .conNivelDificultad(dto.nivelDificultad() != null ? dto.nivelDificultad() : "Medio")
                .conEstado(QuestionStatus.BORRADOR)
                .conAutorId(dto.autorId())
                .build();

        Question guardada = repository.save(question);
        return ResponseEntity.ok(toMap(guardada));
    }

    @GetMapping
    @Operation(summary = "Listar preguntas con filtros y paginación (HU3)", description = "Filtra por competencia, tema, dificultad, estado y autor, con paginación.")
    public ResponseEntity<?> listQuestions(
            @RequestParam(value = "competencia", required = false) String competencia,
            @RequestParam(value = "tema", required = false) String tema,
            @RequestParam(value = "nivel", required = false) String nivel,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "autorId", required = false) Integer autorId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        List<Question> list = repository.findWithFilters(competencia, tema, nivel, estado, autorId, page, size);
        long total = repository.countWithFilters(competencia, tema, nivel, estado, autorId);

        List<Map<String, Object>> content = list.stream().map(this::toMap).toList();
        return ResponseEntity.ok(Map.of(
                "totalElementos", total,
                "pagina", page,
                "tamanio", size,
                "totalPaginas", (int) Math.ceil((double) total / size),
                "preguntas", content
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de pregunta con color de estado (HU2)", description = "Retorna la pregunta con el código hexadecimal del estado según el Patrón State.")
    public ResponseEntity<?> getQuestionById(@PathVariable("id") int id) {
        Optional<Question> opt = repository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toMap(opt.get()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar pregunta")
    public ResponseEntity<?> updateQuestion(
            @PathVariable("id") int id,
            @RequestBody UpdateQuestionRequest request
    ) {
        Optional<Question> opt = repository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (request.estado() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El estado es obligatorio"));
        }
        ValidationRequest validationRequest = new ValidationRequest(
                request.nombre(), request.texto(), request.contexto(), request.opciones(),
                request.respuestaCorrecta(), request.justificacion(), request.bibliografia(),
                request.competencia(), request.tema(), request.subtema(), request.nivelDificultad()
        );
        ValidationResult result = validationFacade.validateQuestion(validationRequest);
        if (!result.valid()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Validación estructural fallida",
                    "detalles", result.errors()
            ));
        }

        Question question = opt.get();
        try {
            QuestionStatus requestedStatus = QuestionStatus.valueOf(request.estado());
            if (question.getEstado() != requestedStatus
                    && requestedStatus == QuestionStatus.PENDIENTE_REVISION
                    && question.getEstado() == QuestionStatus.BORRADOR) {
                question.getStateObject().enviarARevision(question);
            } else if (question.getEstado() != requestedStatus) {
                question.setEstado(requestedStatus);
            }
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", "El estado indicado no es válido"));
        }
        question.setNombre(request.nombre());
        question.setTexto(request.texto());
        question.setContexto(request.contexto());
        question.setOpciones(request.opciones());
        question.setRespuestaCorrecta(request.respuestaCorrecta());
        question.setJustificacion(request.justificacion());
        question.setBibliografia(request.bibliografia());
        question.setCompetencia(request.competencia());
        question.setTema(request.tema());
        question.setSubtema(request.subtema());
        question.setNivelDificultad(request.nivelDificultad());
        question.setRevisorAsignado(request.revisorAsignado());
        question.setAutorId(request.autorId());
        repository.update(question);
        return ResponseEntity.ok(toMap(question));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar pregunta")
    public ResponseEntity<Void> deleteQuestion(@PathVariable("id") int id) {
        if (repository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repository.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/enviar-revision")
    @Operation(summary = "Enviar pregunta a revisión (HU2)", description = "Transiciona de Borrador a Pendiente de Revisión y publica un evento al servicio de notificaciones.")
    public ResponseEntity<?> sendToReview(@PathVariable("id") int id) {
        Optional<Question> opt = repository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Question q = opt.get();
        QuestionState state = q.getStateObject();
        try {
            state.enviarARevision(q);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        repository.update(q);

        // Disparo de evento de dominio (EDA / Patrón Observer)
        eventPublisher.publish(new QuestionSubmittedEvent(q.getId(), q.getNombre(), q.getAutorId()));

        return ResponseEntity.ok(toMap(q));
    }

    @PutMapping("/{id}/reviewers")
    @Operation(summary = "Actualizar asignación de revisores", description = "Operación interna para el contexto de revisión.")
    public ResponseEntity<?> assignReviewers(
            @PathVariable("id") int id,
            @RequestBody AssignReviewersRequest request
    ) {
        if (request.reviewerEmails() == null || request.reviewerEmails().isEmpty()
                || request.reviewerEmails().size() > 3) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe asignarse entre 1 y 3 revisores"));
        }
        Optional<Question> opt = repository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Question question = opt.get();
        try {
            question.getStateObject().asignarRevisores(question, String.join(", ", request.reviewerEmails()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        }
        repository.update(question);
        return ResponseEntity.ok(toMap(question));
    }

    @PutMapping("/{id}/evaluation")
    @Operation(summary = "Registrar evaluación", description = "Operación interna para actualizar el estado desde el contexto de revisión.")
    public ResponseEntity<?> evaluateQuestion(
            @PathVariable("id") int id,
            @RequestBody EvaluateQuestionRequest request
    ) {
        if (request.observation() == null || request.observation().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Las observaciones son obligatorias"));
        }
        Optional<Question> opt = repository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Question question = opt.get();
        String decision = request.decision() == null ? "" : request.decision().trim().toUpperCase(Locale.ROOT);
        try {
            if ("APROBADA".equals(decision)) {
                question.getStateObject().aprobar(question, request.observation());
            } else if ("RECHAZADA".equals(decision)) {
                question.getStateObject().rechazar(question, request.observation());
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "La decisión debe ser APROBADA o RECHAZADA"));
            }
        } catch (IllegalStateException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        }
        repository.update(question);
        return ResponseEntity.ok(toMap(question));
    }

    private Map<String, Object> toMap(Question q) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", q.getId());
        map.put("nombre", q.getNombre());
        map.put("texto", q.getTexto());
        map.put("opciones", q.getOpciones());
        map.put("respuestaCorrecta", q.getRespuestaCorrecta());
        map.put("estado", q.getEstado().name());
        map.put("estadoNombre", q.getStateObject().getNombre());
        map.put("colorHex", q.getColorHex()); // Requisito HU2: colores
        map.put("permiteEdicion", q.getStateObject().permiteEdicion());
        map.put("contexto", q.getContexto());
        map.put("justificacion", q.getJustificacion());
        map.put("bibliografia", q.getBibliografia());
        map.put("competencia", q.getCompetencia());
        map.put("tema", q.getTema());
        map.put("subtema", q.getSubtema());
        map.put("nivelDificultad", q.getNivelDificultad());
        map.put("revisorAsignado", q.getRevisorAsignado());
        map.put("autorId", q.getAutorId());
        return map;
    }
}
