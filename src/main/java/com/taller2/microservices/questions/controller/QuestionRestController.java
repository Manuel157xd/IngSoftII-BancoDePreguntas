package com.taller2.microservices.questions.controller;

import com.taller2.microservices.events.DomainEventBus;
import com.taller2.microservices.events.model.QuestionSubmittedEvent;
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
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "Contexto: Gestión de Preguntas (Question Management)", description = "Microservicio para creación DCE, filtros, paginación y ciclo de vida de preguntas")
@CrossOrigin(origins = "*")
public class QuestionRestController {

    private final QuestionMicroserviceRepository repository;
    private final StructuralValidationFacade validationFacade;
    private final DomainEventBus eventBus;

    public QuestionRestController(QuestionMicroserviceRepository repository,
                                  StructuralValidationFacade validationFacade,
                                  DomainEventBus eventBus) {
        this.repository = repository;
        this.validationFacade = validationFacade;
        this.eventBus = eventBus;
    }

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
            @RequestParam(required = false) String competencia,
            @RequestParam(required = false) String tema,
            @RequestParam(required = false) String nivel,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer autorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
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
    public ResponseEntity<?> getQuestionById(@PathVariable int id) {
        Optional<Question> opt = repository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toMap(opt.get()));
    }

    @PatchMapping("/{id}/enviar-revision")
    @Operation(summary = "Enviar pregunta a revisión (HU2)", description = "Transiciona de Borrador a Pendiente de Revisión aplicando el Patrón State y emite evento asíncrono.")
    public ResponseEntity<?> sendToReview(@PathVariable int id) {
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
        eventBus.publish(new QuestionSubmittedEvent(q.getId(), q.getNombre(), q.getAutorId()));

        return ResponseEntity.ok(toMap(q));
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
