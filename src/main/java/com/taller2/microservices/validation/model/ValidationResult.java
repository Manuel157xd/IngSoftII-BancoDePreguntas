package com.taller2.microservices.validation.model;

import java.util.List;

public record ValidationResult(boolean valid, List<ValidationError> errors) {
    public static ValidationResult ok() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult failure(List<ValidationError> errors) {
        return new ValidationResult(false, errors != null ? List.copyOf(errors) : List.of());
    }
}
