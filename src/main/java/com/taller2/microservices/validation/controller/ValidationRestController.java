package com.taller2.microservices.validation.controller;

import com.taller2.microservices.validation.facade.StructuralValidationFacade;
import com.taller2.microservices.validation.model.ValidationRequest;
import com.taller2.microservices.validation.model.ValidationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/validation")
@Tag(name = "Contexto: Validación Estructural", description = "Servicio de validación estructural de preguntas basadas en Evidencia (DCE)")
@CrossOrigin(origins = "*")
public class ValidationRestController {

    private final StructuralValidationFacade validationFacade;

    public ValidationRestController(StructuralValidationFacade validationFacade) {
        this.validationFacade = validationFacade;
    }

    @PostMapping("/validate")
    @Operation(summary = "Validar estructura de una pregunta", description = "Aplica reglas DCE: 4 distractores, contexto, justificación, bibliografía y taxonomía.")
    public ResponseEntity<ValidationResult> validateQuestion(@RequestBody ValidationRequest request) {
        ValidationResult result = validationFacade.validateQuestion(request);
        if (result.valid()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }
}
