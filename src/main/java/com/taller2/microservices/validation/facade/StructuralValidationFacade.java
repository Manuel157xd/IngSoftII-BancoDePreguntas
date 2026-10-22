package com.taller2.microservices.validation.facade;

import com.taller2.microservices.validation.model.ValidationError;
import com.taller2.microservices.validation.model.ValidationRequest;
import com.taller2.microservices.validation.model.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Patrón Estructural GoF: Facade
 * Ofrece una interfaz unificada y simple para ejecutar todas las reglas de validación
 * estructural de preguntas según los principios del Diseño Centrado en Evidencia (DCE).
 */
@Service
public class StructuralValidationFacade {

    public ValidationResult validateQuestion(ValidationRequest req) {
        if (req == null) {
            return ValidationResult.failure(List.of(new ValidationError("request", "La solicitud no puede ser nula", "FATAL")));
        }

        List<ValidationError> errors = new ArrayList<>();

        // 1. Enunciado y nombre
        if (req.nombre() == null || req.nombre().trim().length() < 3) {
            errors.add(new ValidationError("nombre", "El nombre identificador de la pregunta debe tener al menos 3 caracteres", "ERROR"));
        }
        if (req.enunciado() == null || req.enunciado().trim().length() < 5) {
            errors.add(new ValidationError("enunciado", "La pregunta directa debe tener al menos 5 caracteres", "ERROR"));
        }

        // 2. Contexto DCE
        if (req.contexto() == null || req.contexto().trim().length() < 10) {
            errors.add(new ValidationError("contexto", "El contexto DCE es obligatorio y debe tener al menos 10 caracteres", "ERROR"));
        }

        // 3. Exactamente cuatro distractores (Requisito HU1)
        if (req.opciones() == null || req.opciones().size() != 4) {
            errors.add(new ValidationError("opciones", "La pregunta debe tener exactamente 4 opciones de respuesta", "ERROR"));
        } else {
            for (int i = 0; i < req.opciones().size(); i++) {
                String op = req.opciones().get(i);
                if (op == null || op.trim().isEmpty()) {
                    errors.add(new ValidationError("opciones[" + i + "]", "La opción #" + (i + 1) + " no puede estar vacía", "ERROR"));
                }
            }
        }

        // 4. Índice de respuesta correcta
        if (req.respuestaCorrecta() < 0 || req.respuestaCorrecta() > 3) {
            errors.add(new ValidationError("respuestaCorrecta", "El índice de la respuesta correcta debe estar entre 0 y 3", "ERROR"));
        }

        // 5. Justificación y Bibliografía
        if (req.justificacion() == null || req.justificacion().trim().length() < 5) {
            errors.add(new ValidationError("justificacion", "La justificación de la respuesta correcta es obligatoria", "ERROR"));
        }
        if (req.bibliografia() == null || req.bibliografia().trim().isEmpty()) {
            errors.add(new ValidationError("bibliografia", "La bibliografía de referencia es obligatoria", "WARNING"));
        }

        // 6. Taxonomía pedagógica
        if (req.competencia() == null || req.competencia().trim().isEmpty()) {
            errors.add(new ValidationError("competencia", "La competencia evaluada es requerida", "ERROR"));
        }
        if (req.tema() == null || req.tema().trim().isEmpty()) {
            errors.add(new ValidationError("tema", "El tema es requerido", "ERROR"));
        }

        if (errors.isEmpty()) {
            return ValidationResult.ok();
        }
        return ValidationResult.failure(errors);
    }
}
